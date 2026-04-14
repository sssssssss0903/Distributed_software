#!/bin/bash
# Nacos配置初始化脚本
# 使用方法: bash init-nacos-config.sh [nacos_addr]
# 默认地址: localhost:8848

NACOS_ADDR=${1:-localhost:8848}
NAMESPACE="dev"

echo "========================================"
echo "  Nacos配置初始化"
echo "  地址: ${NACOS_ADDR}"
echo "========================================"

# 1. 创建命名空间 (dev)
echo ""
echo "[1/4] 创建命名空间: dev"
curl -s -X POST "http://${NACOS_ADDR}/nacos/v1/console/namespaces" \
  -d "customNamespaceId=dev&namespaceName=dev&namespaceDesc=开发环境" || true

# 2. 发布共享配置
echo ""
echo "[2/4] 发布共享配置: seckill-common.yaml"
COMMON_CONFIG=$(cat "$(dirname "$0")/seckill-common.yaml")
curl -s -X POST "http://${NACOS_ADDR}/nacos/v1/cs/configs" \
  -d "tenant=dev&dataId=seckill-common.yaml&group=DEFAULT_GROUP&type=yaml&content=${COMMON_CONFIG}"

# 3. 发布网关限流规则
echo ""
echo "[3/4] 发布网关限流规则: seckill-gateway-flow-rules.json"
FLOW_RULES=$(cat "$(dirname "$0")/seckill-gateway-flow-rules.json")
curl -s -X POST "http://${NACOS_ADDR}/nacos/v1/cs/configs" \
  -d "tenant=dev&dataId=seckill-gateway-flow-rules.json&group=DEFAULT_GROUP&type=json&content=${FLOW_RULES}"

# 4. 发布网关降级规则
echo ""
echo "[4/4] 发布网关降级规则: seckill-gateway-degrade-rules.json"
DEGRADE_RULES=$(cat "$(dirname "$0")/seckill-gateway-degrade-rules.json")
curl -s -X POST "http://${NACOS_ADDR}/nacos/v1/cs/configs" \
  -d "tenant=dev&dataId=seckill-gateway-degrade-rules.json&group=DEFAULT_GROUP&type=json&content=${DEGRADE_RULES}"

echo ""
echo "========================================"
echo "  初始化完成！"
echo "  Nacos控制台: http://${NACOS_ADDR}/nacos"
echo "  账号/密码: nacos/nacos"
echo "========================================"
