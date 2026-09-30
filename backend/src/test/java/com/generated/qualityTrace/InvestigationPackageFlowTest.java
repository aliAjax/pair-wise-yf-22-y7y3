package com.generated.qualityTrace;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 冻结调查包核心流程集成测试：
 * 认证/RBAC、冻结幂等、断点续传、现场补录隔离、下一包、受限打码、离线清单与核对。
 */
@SpringBootTest
@TestPropertySource(properties = {"jwt.secret=test-secret", "jwt.ttl-hours=1"})
class InvestigationPackageFlowTest {

  @Autowired
  private WebApplicationContext context;

  private final ObjectMapper mapper = new ObjectMapper();
  private MockMvc mvc;
  private String auditorToken;
  private String inspectorToken;
  private String restrictedToken;

  @BeforeEach
  void setUp() throws Exception {
    mvc = MockMvcBuilders.webAppContextSetup(context).build();
    auditorToken = login("auditor", "auditor123");
    inspectorToken = login("inspector", "insp123");
    restrictedToken = login("restricted", "restricted123");
  }

  private String login(String username, String password) throws Exception {
    MvcResult result = mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().isOk())
        .andReturn();
    return mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
  }

  private JsonNode postJson(String path, String token, String body) throws Exception {
    MvcResult result = mvc.perform(post(path)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body == null ? "{}" : body))
        .andExpect(status().isOk())
        .andReturn();
    return mapper.readTree(result.getResponse().getContentAsString());
  }

  private JsonNode getJson(String path, String token) throws Exception {
    MvcResult result = mvc.perform(get(path).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andReturn();
    return mapper.readTree(result.getResponse().getContentAsString());
  }

  @Test
  void freeze_resume_freezeIsolation_nextPackage_restrictedView_manifest() throws Exception {
    // 未认证 401
    mvc.perform(post("/api/investigation-packages")
            .contentType(MediaType.APPLICATION_JSON).content("{\"workOrderId\":1001}"))
        .andExpect(status().isUnauthorized());

    // 冻结开包
    JsonNode frozen = postJson("/api/investigation-packages", auditorToken, "{\"workOrderId\":1001}");
    String packageNo = frozen.get("packageNo").asText();
    org.junit.jupiter.api.Assertions.assertEquals("OPEN", frozen.get("status").asText());

    // 幂等：同批号
    JsonNode again = postJson("/api/investigation-packages", auditorToken, "{\"workOrderId\":1001}");
    org.junit.jupiter.api.Assertions.assertEquals(packageNo, again.get("packageNo").asText());

    // 注入一次分片失败
    JsonNode failed = postJson("/api/investigation-packages/" + packageNo + "/export",
        auditorToken, "{\"failShardCode\":\"defect\"}");
    org.junit.jupiter.api.Assertions.assertEquals("FAILED",
        failed.get("package").get("status").asText());
    org.junit.jupiter.api.Assertions.assertEquals(1, failed.get("resume").get("failed").asInt());

    // 断点续传：跳过 4、补完 1
    JsonNode resumed = postJson("/api/investigation-packages/" + packageNo + "/export",
        auditorToken, "{}");
    org.junit.jupiter.api.Assertions.assertEquals("COMPLETED",
        resumed.get("package").get("status").asText());
    org.junit.jupiter.api.Assertions.assertEquals(4, resumed.get("resume").get("doneSkipped").asInt());
    org.junit.jupiter.api.Assertions.assertEquals(1, resumed.get("resume").get("succeeded").asInt());

    // 现场补录：当前包隔离
    postJson("/api/quality-inspections", inspectorToken,
        "{\"batchId\":2001,\"inspectionType\":\"FINAL\",\"standardVersion\":\"STD-AXLE-v3.2\",\"resultStatus\":\"FAIL\"}");
    postJson("/api/defects", inspectorToken,
        "{\"batchId\":2001,\"defectType\":\"DENT\",\"defectQty\":2,\"severity\":\"MAJOR\",\"rootCause\":\"磕碰\",\"dispositionStatus\":\"OPEN\"}");
    JsonNode detail = getJson("/api/investigation-packages/" + packageNo, auditorToken);
    org.junit.jupiter.api.Assertions.assertEquals(1,
        detail.get("nextPackageHint").get("newInspectionCountAfterFreeze").asInt());
    org.junit.jupiter.api.Assertions.assertEquals(1,
        detail.get("nextPackageHint").get("newDefectCountAfterFreeze").asInt());
    org.junit.jupiter.api.Assertions.assertEquals(1, detail.get("inspections").size());

    // 下一包包含补录
    JsonNode next = postJson("/api/investigation-packages/" + packageNo + "/next", auditorToken, null);
    String nextNo = next.get("packageNo").asText();
    org.junit.jupiter.api.Assertions.assertNotEquals(packageNo, nextNo);
    postJson("/api/investigation-packages/" + nextNo + "/export", auditorToken, "{}");
    MvcResult defectsBlob = mvc.perform(
            get("/api/investigation-packages/" + nextNo + "/shards/defect")
                .header("Authorization", "Bearer " + auditorToken))
        .andExpect(status().isOk()).andReturn();
    JsonNode defectRecords = mapper.readTree(defectsBlob.getResponse().getContentAsString()).get("records");
    org.junit.jupiter.api.Assertions.assertEquals(2, defectRecords.size());

    // 受限检验员：只看代号
    JsonNode restricted = getJson("/api/investigation-packages/" + packageNo, restrictedToken);
    org.junit.jupiter.api.Assertions.assertTrue(restricted.get("restricted").asBoolean());
    org.junit.jupiter.api.Assertions.assertEquals("WO-20260920-1001",
        restricted.get("workOrder").get("orderNo").asText());
    org.junit.jupiter.api.Assertions.assertEquals("****",
        restricted.get("workOrder").get("productName").asText());
    org.junit.jupiter.api.Assertions.assertEquals("DIM-01",
        restricted.get("inspections").get(0).get("itemResults").get(0).get("itemCode").asText());
    org.junit.jupiter.api.Assertions.assertFalse(
        restricted.get("inspections").get(0).get("itemResults").get(0).has("measuredValue")
            && !"****".equals(restricted.get("inspections").get(0).get("itemResults").get(0)
                .get("measuredValue").asText()));
    mvc.perform(get("/api/investigation-packages/" + packageNo + "/shards/defect")
            .header("Authorization", "Bearer " + restrictedToken))
        .andExpect(status().isForbidden());

    // 离线核对
    JsonNode verify = postJson("/api/investigation-packages/" + packageNo + "/verify", auditorToken, "{}");
    org.junit.jupiter.api.Assertions.assertTrue(verify.get("allShardsMatch").asBoolean());
  }

  @Test
  void rbac_deniesManagerPackageAndFreezeForRestricted() throws Exception {
    String managerToken = login("manager", "mgr123");
    mvc.perform(post("/api/investigation-packages")
            .header("Authorization", "Bearer " + managerToken)
            .contentType(MediaType.APPLICATION_JSON).content("{\"workOrderId\":1001}"))
        .andExpect(status().isForbidden());
    mvc.perform(post("/api/investigation-packages")
            .header("Authorization", "Bearer " + restrictedToken)
            .contentType(MediaType.APPLICATION_JSON).content("{\"workOrderId\":1001}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void missingReasons_areListedOffline_forIncompleteWorkOrders() throws Exception {
    String p2 = postJson("/api/investigation-packages", auditorToken, "{\"workOrderId\":1002}")
        .get("packageNo").asText();
    postJson("/api/investigation-packages/" + p2 + "/export", auditorToken, "{}");
    MvcResult manifest = mvc.perform(
            get("/api/investigation-packages/" + p2 + "/manifest")
                .header("Authorization", "Bearer " + auditorToken))
        .andExpect(status().isOk()).andReturn();
    String body = manifest.getResponse().getContentAsString();
    org.junit.jupiter.api.Assertions.assertTrue(body.contains("NO_INSPECTION_ITEM"));
    org.junit.jupiter.api.Assertions.assertTrue(body.contains("NO_DEFECT"));
    org.junit.jupiter.api.Assertions.assertTrue(body.contains("STABLE SECTION BEGIN"));

    mvc.perform(post("/api/investigation-packages")
            .header("Authorization", "Bearer " + auditorToken)
            .contentType(MediaType.APPLICATION_JSON).content("{\"workOrderId\":9999}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("WORK_ORDER_NOT_FOUND"));
  }
}
