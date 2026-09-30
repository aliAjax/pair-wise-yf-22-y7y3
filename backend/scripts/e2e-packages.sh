#!/usr/bin/env bash
# 冻结调查包端到端核验脚本
set -u
BASE=${BASE:-http://localhost:8080}
PASS=0; FAIL=0

ok()   { if [ "$1" = "$2" ]; then PASS=$((PASS+1)); echo "  PASS: $3"; else FAIL=$((FAIL+1)); echo "  FAIL: $3 (expected=$2 got=$1)"; fi; }
code() { curl -s -o /dev/null -w '%{http_code}' "$@"; }
# jget: 从 stdin JSON 提取字段（点路径）；索引越界返回空串
jget() { python3 -c '
import sys,json
d=json.load(sys.stdin)
for p in sys.argv[1].split("."):
    try:
        d = d[int(p)] if p.isdigit() and isinstance(d,list) else d[p]
    except (KeyError, IndexError, ValueError):
        d = None
        break
print("" if d is None else d)' "$1"; }
# mbody: 从离线清单文件中截取 STABLE SECTION 并输出 JSON
mbody() { python3 -c '
import sys,json
raw=sys.stdin.read()
b="# === STABLE SECTION BEGIN ==="
e="# === STABLE SECTION END ==="
section=raw[raw.index(b)+len(b):raw.index(e)].strip()
print(json.dumps(json.loads(section), ensure_ascii=False))'; }
passfail() { if [ "$1" = "$2" ]; then PASS=$((PASS+1)); echo "  PASS: $3"; else FAIL=$((FAIL+1)); echo "  FAIL: $3 (expected=$2 got=$1)"; fi; }

login() { curl -s -X POST "$BASE/api/auth/login" -H 'Content-Type: application/json' -d "{\"username\":\"$1\",\"password\":\"$2\"}" | jget token; }

AUD=$(login auditor auditor123)
INSP=$(login inspector insp123)
REST=$(login restricted restricted123)
MGR=$(login manager mgr123)

echo "== 1. 未认证拒绝 =="
ok "$(code -X POST "$BASE/api/investigation-packages" -H 'Content-Type: application/json' -d '{"workOrderId":1001}')" 401 "无 token 返回 401"

echo "== 2. 质量经理无权访问调查包 (RBAC) =="
ok "$(code -X POST "$BASE/api/investigation-packages" -H "Authorization: Bearer $MGR" -H 'Content-Type: application/json' -d '{"workOrderId":1001}')" 403 "QUALITY_MANAGER 被拒"

echo "== 3. 受限检验员不能冻结（只读） =="
ok "$(code -X POST "$BASE/api/investigation-packages" -H "Authorization: Bearer $REST" -H 'Content-Type: application/json' -d '{"workOrderId":1001}')" 403 "RESTRICTED_INSPECTOR 写操作被拒"

echo "== 4. 审计员冻结开包 1001 =="
FREEZE=$(curl -s -X POST "$BASE/api/investigation-packages" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json' -d '{"workOrderId":1001}')
PNO=$(echo "$FREEZE" | jget packageNo)
ok "$(echo "$FREEZE" | jget status)" "OPEN" "开包状态 OPEN"
echo "  packageNo=$PNO"; [ -n "$PNO" ] && PASS=$((PASS+1))

echo "== 5. 幂等：重复申请同一工单取回同一批号 =="
PNO2=$(curl -s -X POST "$BASE/api/investigation-packages" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json' -d '{"workOrderId":1001}' | jget packageNo)
ok "$PNO2" "$PNO" "重复申请批号一致"

echo "== 6. 导出失败注入（defect 分片失败一次） =="
EXP1=$(curl -s -X POST "$BASE/api/investigation-packages/$PNO/export" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json' -d '{"failShardCode":"defect"}')
ok "$(echo "$EXP1" | jget package.status)" "FAILED" "首导出 FAILED"
ok "$(echo "$EXP1" | jget resume.doneSkipped)" 0 "首次无已完成分片"
ok "$(echo "$EXP1" | jget resume.failed)" 1 "恰好 1 个失败"
ok "$(echo "$EXP1" | jget resume.failedShardCodes.0)" "defect" "失败分片码 defect"

echo "== 7. 断点续传：跳过已完成，重试失败分片 =="
EXP2=$(curl -s -X POST "$BASE/api/investigation-packages/$PNO/export" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json' -d '{}')
ok "$(echo "$EXP2" | jget package.status)" "COMPLETED" "续传后 COMPLETED"
ok "$(echo "$EXP2" | jget resume.doneSkipped)" 4 "续传跳过 4 个已完成分片"
ok "$(echo "$EXP2" | jget resume.succeeded)" 1 "续传补完 1 个分片"

echo "== 8. 再次导出全部命中已完成分片 =="
EXP3=$(curl -s -X POST "$BASE/api/investigation-packages/$PNO/export" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json' -d '{}')
ok "$(echo "$EXP3" | jget resume.doneSkipped)" 5 "全部 5 个分片从已完成状态恢复"

echo "== 9. 冻结后现场补录：不进当前包，预告进入下一包 =="
NEWINSP=$(curl -s -X POST "$BASE/api/quality-inspections" -H "Authorization: Bearer $INSP" -H 'Content-Type: application/json' -d '{"batchId":2001,"inspectionType":"FINAL","standardVersion":"STD-AXLE-v3.2","resultStatus":"FAIL"}')
[ -n "$(echo "$NEWINSP" | jget id)" ] && PASS=$((PASS+1)) && echo "  PASS: 现场补录检验成功" || { FAIL=$((FAIL+1)); echo "  FAIL: 补录检验 $NEWINSP"; }
NEWDEF=$(curl -s -X POST "$BASE/api/defects" -H "Authorization: Bearer $INSP" -H 'Content-Type: application/json' -d '{"batchId":2001,"defectType":"DENT","defectQty":2,"severity":"MAJOR","rootCause":"磕碰","dispositionStatus":"OPEN"}')
[ -n "$(echo "$NEWDEF" | jget id)" ] && PASS=$((PASS+1)) && echo "  PASS: 现场补录不良成功" || { FAIL=$((FAIL+1)); echo "  FAIL: 补录不良 $NEWDEF"; }

DETAIL=$(curl -s "$BASE/api/investigation-packages/$PNO" -H "Authorization: Bearer $AUD")
ok "$(echo "$DETAIL" | jget nextPackageHint.newInspectionCountAfterFreeze)" 1 "下一包预告：1 条新检验"
ok "$(echo "$DETAIL" | jget nextPackageHint.newDefectCountAfterFreeze)" 1 "下一包预告：1 条新不良"

OLDDEFCHK=$(curl -s "$BASE/api/investigation-packages/$PNO" -H "Authorization: Bearer $AUD" | python3 -c 'import sys,json;d=json.load(sys.stdin);print([s["checksumSha256"] for s in d["shards"] if s["shardCode"]=="defect"][0])')
curl -s -X POST "$BASE/api/investigation-packages/$PNO/export" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json' -d '{}' >/dev/null
NEWDEFCHK=$(curl -s "$BASE/api/investigation-packages/$PNO" -H "Authorization: Bearer $AUD" | python3 -c 'import sys,json;d=json.load(sys.stdin);print([s["checksumSha256"] for s in d["shards"] if s["shardCode"]=="defect"][0])')
ok "$NEWDEFCHK" "$OLDDEFCHK" "冻结包不良分片摘要不随补录改变"
OLDALL=$(echo "$DETAIL" | python3 -c 'import sys,json;d=json.load(sys.stdin);print(len(d["inspections"]))')
ok "$OLDALL" 1 "当前包仍只有冻结时的 1 条检验（新检验被隔离）"

echo "== 10. 开下一包：批号 +1 且包含补录内容 =="
NEXT=$(curl -s -X POST "$BASE/api/investigation-packages/$PNO/next" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json')
NPN=$(echo "$NEXT" | jget packageNo)
[ "$NPN" != "$PNO" ] && PASS=$((PASS+1)) && echo "  PASS: 下一包批号不同 ($NPN)" || { FAIL=$((FAIL+1)); echo "  FAIL: 下一包批号相同"; }
ok "$(echo "$NEXT" | jget sequence)" 2 "下一包序号为 2"
curl -s -X POST "$BASE/api/investigation-packages/$NPN/export" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json' -d '{}' >/dev/null
NEXT_DEFCOUNT=$(curl -s "$BASE/api/investigation-packages/$NPN/shards/defect" -H "Authorization: Bearer $AUD" | python3 -c 'import sys,json;print(len(json.load(sys.stdin)["records"]))')
ok "$NEXT_DEFCOUNT" 2 "下一包不良分片含 2 条（旧 1 + 新 1）"
NEXT_INSPCOUNT=$(curl -s "$BASE/api/investigation-packages/$NPN/shards/inspection" -H "Authorization: Bearer $AUD" | python3 -c 'import sys,json;print(len(json.load(sys.stdin)["records"]))')
ok "$NEXT_INSPCOUNT" 2 "下一包检验分片含 2 条（旧 1 + 新 1）"

echo "== 11. 受限检验员视图：只有代号，无敏感值 =="
RDETAIL=$(curl -s "$BASE/api/investigation-packages/$PNO" -H "Authorization: Bearer $REST")
ok "$(echo "$RDETAIL" | jget restricted)" "True" "受限视图标记"
ok "$(echo "$RDETAIL" | jget workOrder.orderNo)" "WO-20260920-1001" "可核验工单代号可见"
BATCHNO=$(echo "$RDETAIL" | python3 -c 'import sys,json;print(json.load(sys.stdin)["batches"][0]["batchNo"])')
ok "$BATCHNO" "B-1001-01" "可核验批次代号可见"
if echo "$RDETAIL" | grep -q '驱动轴总成'; then FAIL=$((FAIL+1)); echo "  FAIL: 受限身份看到产品名称"; else PASS=$((PASS+1)); echo "  PASS: 受限身份看不到产品名称"; fi
if echo "$RDETAIL" | grep -q '25.02'; then FAIL=$((FAIL+1)); echo "  FAIL: 受限身份看到测量值"; else PASS=$((PASS+1)); echo "  PASS: 受限身份看不到测量值"; fi
ITEMCODE=$(echo "$RDETAIL" | python3 -c 'import sys,json;d=json.load(sys.stdin);print(d["inspections"][0]["itemResults"][0]["itemCode"])')
ok "$ITEMCODE" "DIM-01" "受限身份仍可核验检验项代号"
ITEMNAME=$(echo "$RDETAIL" | python3 -c 'import sys,json;d=json.load(sys.stdin);print(d["inspections"][0]["itemResults"][0].get("itemName","<absent>"))')
ok "$ITEMNAME" "<absent>" "受限身份检验项名称不下发"
ok "$(code "$BASE/api/investigation-packages/$PNO/shards/defect" -H "Authorization: Bearer $REST")" 403 "受限身份下载原始分片被拒"

echo "== 12. 审计员可下载原始分片 =="
ok "$(code "$BASE/api/investigation-packages/$PNO/shards/defect" -H "Authorization: Bearer $AUD")" 200 "审计员下载原始分片 200"

echo "== 13. 离线清单：数量 / 摘要 / 缺件原因（1001 完整） =="
MAN=$(curl -s "$BASE/api/investigation-packages/$PNO/manifest" -H "Authorization: Bearer $AUD")
BODY=$(echo "$MAN" | mbody)
ok "$(echo "$BODY" | jget summary.recordTotal)" 6 "清单记录总数=6(1工单+1批次+1检验+2项+1不良)"
ok "$(echo "$BODY" | jget summary.shardDone)" 5 "清单分片完成数=5"
ok "$(echo "$BODY" | jget missingItems.0)" "" "1001 无缺件条目"
[ -n "$(echo "$BODY" | python3 -c 'import sys,json;d=json.load(sys.stdin);print(d["shards"][0]["checksumSha256"])')" ] && PASS=$((PASS+1)) && echo "  PASS: 清单含分片 SHA-256" || { FAIL=$((FAIL+1)); echo "  FAIL: 清单缺分片摘要"; }
echo "$MAN" | head -3 | grep -q "离线核对清单" && PASS=$((PASS+1)) && echo "  PASS: 清单带离线文本头" || { FAIL=$((FAIL+1)); echo "  FAIL: 缺离线文本头"; }
# 离线重算稳定正文摘要，与头部声明一致
LOCAL_SUM=$(echo "$MAN" | python3 -c '
import sys,hashlib
raw=sys.stdin.read()
b="# === STABLE SECTION BEGIN ==="; e="# === STABLE SECTION END ==="
section=raw[raw.index(b)+len(b):raw.index(e)].strip()
print(hashlib.sha256(section.encode()).hexdigest())
')
DECLARED=$(echo "$MAN" | sed -n '2s/^# 清单稳定正文摘要 SHA-256: //p')
ok "$LOCAL_SUM" "$DECLARED" "离线重算 STABLE SECTION 摘要与头部一致"

echo "== 14. 工单 1002 缺检验项 + 无不良 =="
P2=$(curl -s -X POST "$BASE/api/investigation-packages" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json' -d '{"workOrderId":1002}' | jget packageNo)
curl -s -X POST "$BASE/api/investigation-packages/$P2/export" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json' -d '{}' >/dev/null
M2=$(curl -s "$BASE/api/investigation-packages/$P2/manifest" -H "Authorization: Bearer $AUD" | mbody)
echo "$M2" | grep -q NO_INSPECTION_ITEM && PASS=$((PASS+1)) && echo "  PASS: 1002 缺件原因 NO_INSPECTION_ITEM" || { FAIL=$((FAIL+1)); echo "  FAIL: 1002 缺 NO_INSPECTION_ITEM"; }
echo "$M2" | grep -q NO_DEFECT && PASS=$((PASS+1)) && echo "  PASS: 1002 缺件原因 NO_DEFECT" || { FAIL=$((FAIL+1)); echo "  FAIL: 1002 缺 NO_DEFECT"; }
echo "$M2" | grep -q "检验单缺少检验项结果明细" && PASS=$((PASS+1)) && echo "  PASS: 缺件中文原因可离线阅读" || { FAIL=$((FAIL+1)); echo "  FAIL: 缺件中文原因"; }

echo "== 15. 工单 1003 无批次连锁缺件 =="
P3=$(curl -s -X POST "$BASE/api/investigation-packages" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json' -d '{"workOrderId":1003}' | jget packageNo)
curl -s -X POST "$BASE/api/investigation-packages/$P3/export" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json' -d '{}' >/dev/null
M3=$(curl -s "$BASE/api/investigation-packages/$P3/manifest" -H "Authorization: Bearer $AUD" | mbody)
# 因果链：无批次 -> 检验/不良分片根因 NO_BATCH；检验项分片 NO_INSPECTION
python3 -c '
import sys,json
d=json.load(sys.stdin)
mi={(m["shardCode"],m["reasonCode"]) for m in d["missingItems"]}
assert ("batch","NO_BATCH") in mi, mi
assert ("inspection","NO_BATCH") in mi, mi
assert ("inspection_item","NO_INSPECTION") in mi, mi
assert ("defect","NO_BATCH") in mi, mi
assert ("defect","NO_DEFECT") in mi, mi
print("ok")
' <<< "$M3" && PASS=$((PASS+5)) && echo "  PASS: 1003 缺件根因链正确（NO_BATCH→NO_INSPECTION，5 项）" || FAIL=$((FAIL+5))

echo "== 16. 不存在工单 / 未知包 / 非法入参 =="
ok "$(code -X POST "$BASE/api/investigation-packages" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json' -d '{"workOrderId":9999}')" 404 "未知工单 404"
ok "$(code "$BASE/api/investigation-packages/FZ-NOPE-999" -H "Authorization: Bearer $AUD")" 404 "未知批号 404"
ok "$(code -X POST "$BASE/api/investigation-packages" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json' -d '{"workOrderId":"abc"}')" 400 "workOrderId 非数字 400"

echo "== 17. 离线核对 verify =="
VR=$(curl -s -X POST "$BASE/api/investigation-packages/$PNO/verify" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json' -d '{}')
ok "$(echo "$VR" | jget allShardsMatch)" "True" "重算摘要全部一致"
CHK=$(echo "$VR" | jget manifestChecksum)
VR2=$(curl -s -X POST "$BASE/api/investigation-packages/$PNO/verify" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json' -d "{\"expectedManifestChecksum\":\"$CHK\"}")
ok "$(echo "$VR2" | jget manifestChecksumMatch)" "True" "携带现场摘要比对一致"
VR3=$(curl -s -X POST "$BASE/api/investigation-packages/$PNO/verify" -H "Authorization: Bearer $AUD" -H 'Content-Type: application/json' -d '{"expectedManifestChecksum":"deadbeef"}')
ok "$(echo "$VR3" | jget manifestChecksumMatch)" "False" "错误摘要被识别为不一致"

echo "== 18. 质检员可补录但不能访问调查包 =="
ok "$(code "$BASE/api/investigation-packages/$PNO" -H "Authorization: Bearer $INSP")" 403 "INSPECTOR 访问调查包被拒"
ok "$(code -X POST "$BASE/api/defects" -H "Authorization: Bearer $MGR" -H 'Content-Type: application/json' -d '{"batchId":2001,"defectType":"X","defectQty":1,"severity":"MINOR","rootCause":"x","dispositionStatus":"OPEN"}')" 403 "质量经理补录不良被拒"

echo
echo "RESULT: PASS=$PASS FAIL=$FAIL"
exit $FAIL
