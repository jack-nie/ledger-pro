# Ledger Pro

[![Build](https://github.com/jack-nie/ledger-pro/actions/workflows/build.yml/badge.svg)](https://github.com/jack-nie/ledger-pro/actions/workflows/build.yml)
[![Release](https://github.com/jack-nie/ledger-pro/actions/workflows/release.yml/badge.svg)](https://github.com/jack-nie/ledger-pro/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/jack-nie/ledger-pro?display_name=tag)](https://github.com/jack-nie/ledger-pro/releases)
[![Repo](https://img.shields.io/github/repo-size/jack-nie/ledger-pro)](https://github.com/jack-nie/ledger-pro)

一个基于 `Spring Boot + React + MySQL + Redis` 的记账应用，覆盖常见记账软件的核心能力，并补全了登录鉴权、分账、导出和定时记账。

## 功能概览

- 登录鉴权：用户名密码登录，Redis 保存 Token 会话
- 月度仪表盘：收入、支出、结余、储蓄率、账户余额
- 流水中心：新增、编辑、删除、搜索、按类型/分类筛选
- 分账与标签：一笔流水可拆成多条分类明细，并支持标签展示与搜索
- 预算管理：按月为支出分类设置预算并跟踪执行率
- 定时记账：支持每周/每月规则、手动补跑到期规则、自动生成流水
- 统计导出：导出流水 CSV、导出月度仪表盘 CSV
- Redis 缓存：基础下拉数据和仪表盘结果缓存

## 项目结构

```text
ledger-pro
├─ backend   # Spring Boot 2.7 / JPA / MySQL / Redis
├─ frontend  # React 19 / Vite / Recharts
└─ docker-compose.yml
```

## 环境要求

- Java 8
- Node.js 22+
- Docker

## 启动基础设施

在项目根目录执行：

```bash
docker compose up -d
```

会启动：

- MySQL: `localhost:3306`
- Redis: `localhost:6379`

默认数据库配置见 [application.yml](/D:/workspace/ledger-pro/backend/src/main/resources/application.yml)：

- 数据库名：`ledger_pro`
- 用户名：`root`
- 密码：`root123`

## 启动后端

```bash
cd backend
mvnw.cmd spring-boot:run
```

后端地址：

- `http://localhost:8080`

## 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：

- `http://localhost:5173`

开发模式下，Vite 默认代理 `/api` 到 `http://localhost:8080`。如果本机 `8080` 已被占用，可以先指定：

```powershell
$env:LEDGER_BACKEND_URL="http://localhost:8081"
npm run dev
```

## 默认初始化数据

首次启动会自动初始化：

- 默认登录账号：`demo`
- 默认密码：`123456`
- 账户、分类、预算、近 6 个月示例流水
- 一条示例分账流水
- 两条定时记账规则

## 主要接口

认证：

- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`

工作台：

- `GET /api/lookups/bootstrap`
- `GET /api/dashboard`

流水：

- `GET /api/transactions`
- `POST /api/transactions`
- `PUT /api/transactions/{id}`
- `DELETE /api/transactions/{id}`

预算：

- `GET /api/budgets`
- `POST /api/budgets`

定时记账：

- `GET /api/recurring-rules`
- `POST /api/recurring-rules`
- `PUT /api/recurring-rules/{id}`
- `DELETE /api/recurring-rules/{id}`
- `POST /api/recurring-rules/process`

导出：

- `GET /api/exports/transactions.csv`
- `GET /api/exports/dashboard.csv`

## GitHub Actions

已配置两个工作流：

- [build.yml](/D:/workspace/ledger-pro/.github/workflows/build.yml)
  说明：在 `push main`、`pull_request`、手动触发时执行，构建后端和前端，并上传构建产物
- [release.yml](/D:/workspace/ledger-pro/.github/workflows/release.yml)
  说明：在推送 `v*` 标签时自动构建并创建 GitHub Release，附带后端 jar 和前端压缩包

## 本地验证

已完成本地构建验证：

- 后端：`mvnw.cmd -q -DskipTests compile`
- 前端：`npm run build`
