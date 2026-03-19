#!/bin/bash
# 主从复制配置脚本 - 在主库就绪后执行
set -e
MASTER_HOST=${1:-mysql-master}
SLAVE_HOST=${2:-mysql-slave}

echo "Waiting for master..."
until mysql -h"$MASTER_HOST" -uroot -proot -e "SELECT 1" &>/dev/null; do
  sleep 2
done

echo "Getting master status..."
MASTER_STATUS=$(mysql -h"$MASTER_HOST" -uroot -proot -e "SHOW MASTER STATUS\G")
BINLOG_FILE=$(echo "$MASTER_STATUS" | grep "File:" | awk '{print $2}')
# 使用起始位置4以同步主库init阶段的全部数据
BINLOG_POS=4

echo "Master: $BINLOG_FILE @ $BINLOG_POS"

echo "Waiting for slave..."
until mysql -h"$SLAVE_HOST" -uroot -proot -e "SELECT 1" &>/dev/null; do
  sleep 2
done

echo "Configuring replication on slave..."
mysql -h"$SLAVE_HOST" -uroot -proot <<EOF
STOP SLAVE;
CHANGE MASTER TO
  MASTER_HOST='$MASTER_HOST',
  MASTER_USER='repl',
  MASTER_PASSWORD='repl',
  MASTER_LOG_FILE='$BINLOG_FILE',
  MASTER_LOG_POS=$BINLOG_POS;
START SLAVE;
SHOW SLAVE STATUS\G
EOF

echo "Replication configured."
