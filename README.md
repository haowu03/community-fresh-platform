# 社区便民生鲜配送平台

面向社区场景的生鲜电商系统，解决社区生鲜"最后一公里"问题。前后端分离架构，包含**管理端 PC 后台**与**用户端移动 H5** 两套独立前端。

相比普通外卖，生鲜具有保质期短、冷链要求、按重量计价、损耗高、产地溯源等业务特性，本项目衍生出社区自提、预售采摘、临期特价、社区团购等差异化玩法。

## 技术栈

**后端**
- Java 17+ / Spring Boot 3 / MyBatis-Plus / MySQL 8 / Redis（Lettuce）
- JWT 双体系认证（用户端 / 管理端双密钥）+ BCrypt 密码加密
- Knife4j 接口文档（http://localhost:8080/doc.html）
- 定时任务：超时关单、批次临期/过期扫描、预售关闭、团购结算

**前端**
- 管理端：Vue3 + Element Plus + ECharts（PC 后台）
- 用户端：Vue3 + Vant 4（移动端 H5）

## 核心功能

| 模块 | 说明 |
|---|---|
| 商品浏览 | 分类筛选、搜索、Redis 缓存（Cache Aside 模式） |
| 称重购买 | 蔬果鲜肉按重量选购（0.5 斤步进+快捷重量），鱼类/整盒商品按数量整卖 |
| 批次管理 | 批次入库（生产日期+保质期自动算到期日）、FEFO 先到期先出 |
| 临期特价 | 定时扫描批次，临期自动 7 折、过期作废 |
| 双履约 | 配送到家 / 社区自提（自提点+时间段+6 位自提码核销） |
| 预售采摘 | 预售下单、到点自动关闭、发货日集中履约 |
| 社区团购 | 团长发起、满员成团、到期流团自动退款、团长后台 |
| 库存防超卖 | 乐观锁 SQL（`stock >= ?`）+ Redis 下单防重锁 |
| 溯源 | 产地 / 种植户 / 种植采摘日期 / 检测报告 |
| 社区 | 发帖（管理员审核）、评论、点赞 |

## 项目结构

```
delivery platform/
├── fresh-server/          # 后端（Spring Boot 3）
├── fresh-admin/           # 管理端前端（Vue3 + Element Plus）
├── fresh-user/            # 用户端前端（Vue3 + Vant）
├── sql/                   # 建库建表 + 演示数据脚本
│   ├── community_fresh.sql    # 20 张表 DDL + 基础种子数据
│   ├── demo-data.sql          # 演示数据（商品/批次/预售/团购/帖子）
│   └── demo-data2.sql         # 扩展演示数据（历史订单/评论等）
├── upload/                # 商品演示图片（demo SQL 引用）
├── docs/                  # 需求/流程/数据库/接口/运行指南文档
├── docker-compose.yml     # MySQL + Redis 一键启动（可选）
└── start_fresh_front.bat  # 本地一键启动两套前端
```

## 快速开始

### 环境依赖
本项目需要自行安装以下中间件（不会包含在代码仓库内）
1. MySQL8.0
2. Redis（Windows解压版，下载地址：`https://github.com/tporadowski/redis/releases`）

### Redis启动
1. 本地解压Redis到任意目录
2. 执行 redis-server.exe 启动服务（窗口保持打开）
3. 使用 redis-cli ping 验证，返回PONG即成功

> 有 Docker 的话也可以直接 `docker compose up -d` 一键启动 MySQL 8 + Redis（首次自动执行 sql 初始化脚本）。

### 1. 初始化数据库
```bash
mysql -uroot -p123456 < sql/community_fresh.sql
mysql -uroot -p123456 community_fresh < sql/demo-data.sql
mysql -uroot -p123456 community_fresh < sql/demo-data2.sql
```

### 2. 启动后端
IDEA 打开 `fresh-server`，运行 `com.fresh.FreshApplication`（端口 8080）。
数据库账号密码在 `fresh-server/src/main/resources/application.yml` 中修改（默认 root / 123456）。

### 3. 启动前端
双击 `start_fresh_front.bat`，或分别执行：
```bash
cd fresh-admin && npm install && npm run dev   # 管理端 http://localhost:5173
cd fresh-user  && npm install && npm run dev   # 用户端 http://localhost:5174
```

### 演示账号

| 端 | 账号 | 密码 | 角色 |
|---|---|---|---|
| 管理端 | admin | 123456 | 平台管理员 |
| 用户端 | user1 | 123456 | 普通用户 |
| 用户端 | leader1 | 123456 | 团长（绑定自提点） |

## 文档

详细设计文档见 `docs/` 目录：
- [01-需求文档](docs/01-需求文档.md)
- [02-业务流程](docs/02-业务流程.md)
- [03-数据库设计](docs/03-数据库设计.md)
- [04-后端模块划分与接口文档](docs/04-后端模块划分与接口文档.md)
- [05-使用教程与启动指南](docs/05-运行指南.md)
