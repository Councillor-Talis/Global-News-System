#  Global News System

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=flat-square&logo=springboot)
![Vue](https://img.shields.io/badge/Vue.js-3.x-4FC08D?style=flat-square&logo=vue.js)
![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?style=flat-square&logo=mysql)
![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?style=flat-square&logo=redis)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.x-005571?style=flat-square&logo=elasticsearch)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

</div>

---

##  项目简介

**Global News System (GNS)** 是一个现代化的全球新闻聚合与管理平台。系统通过集成 [NewsAPI](https://newsapi.org/) 定时自动采集来自全球各大媒体的新闻，为用户提供流畅、专业的新闻阅读体验。

###  核心特性

-  **自动新闻采集** — 每 15 分钟定时从 NewsAPI 拉取全球各媒体最新资讯
-  **智能分类** — 基于关键词的多策略文章智能分类引擎
-  **全文搜索** — 集成 Elasticsearch，支持高效的关键词全文检索
-  **高性能缓存** — Redis 缓存热点数据，显著提升接口响应速度
-  **互动评论系统** — 支持多级评论、点赞、回复等社区互动功能
-  **JWT 身份认证** — 基于 Spring Security + JWT 的无状态安全认证
-  **管理后台** — 完整的管理员控制台，支持文章、用户、数据源一站式管理
-  **响应式设计** — 现代化 UI，自适应多种屏幕尺寸
 
---

##  技术架构

```
┌─────────────────────────────────────────────┐
│              Frontend (Vue 3)               │
│    Vue Router · Pinia · Element Plus        │
│              Vite · Axios                   │
└──────────────────────┬──────────────────────┘
                       │ HTTP / REST API
┌──────────────────────▼──────────────────────┐
│           Backend (Spring Boot 3)           │
│  Spring Security · JWT · MyBatis-Plus       │
│          Jsoup · RestTemplate               │
└───┬──────────────┬──────────────┬───────────┘
    │              │              │
┌───▼───┐    ┌────▼────┐   ┌─────▼──────┐
│ MySQL │    │  Redis  │   │Elasticsearch│
│  8.x  │    │  7.x    │   │    8.x      │
└───────┘    └─────────┘   └────────────┘
                       │
              ┌────────▼────────┐
              │   NewsAPI.org   │
              └─────────────────┘
```

### 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.0 | 核心框架 |
| Spring Security | 6.x | 权限认证 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| JJWT | 0.12.3 | JWT 令牌 |
| Spring Data Redis | - | 数据缓存 |
| Spring Data Elasticsearch | - | 全文搜索 |
| Jsoup | 1.17.2 | 新闻正文抓取 |
| Fastjson2 | 2.0.43 | JSON 处理 |
| Lombok | - | 代码简化 |

### 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue.js | 3.5.x | 核心框架 |
| Vue Router | 5.x | 路由管理 |
| Pinia | 3.x | 状态管理 |
| Element Plus | 2.x | UI 组件库 |
| Axios | 1.x | HTTP 客户端 |
| Vite | 8.x | 构建工具 |

---

##  项目结构

```
global-news-system/
├── src/main/java/com/gns/
│   ├── GlobalNewsApplication.java   # 启动类
│   ├── controller/                  # REST 控制器
│   │   ├── NewsController.java      # 新闻相关接口
│   │   ├── CommentController.java   # 评论相关接口
│   │   ├── UserController.java      # 用户相关接口
│   │   └── AdminController.java     # 管理员接口
│   ├── entity/                      # 数据库实体
│   │   ├── Article.java             # 文章
│   │   ├── User.java                # 用户
│   │   ├── Comment.java             # 评论
│   │   ├── Category.java            # 分类
│   │   ├── CommentLike.java         # 评论点赞
│   │   └── SourceConfig.java        # 新闻来源配置
│   ├── scheduler/                   # 定时任务
│   │   ├── NewsCrawlerService.java  # 新闻定时采集
│   │   ├── ArticleContentFetcher.java # 正文抓取
│   │   └── ArticleCategorizer.java  # 智能分类引擎
│   ├── search/                      # 搜索模块
│   ├── service/                     # 业务逻辑层
│   ├── mapper/                      # 数据访问层
│   ├── config/                      # 配置类
│   ├── filter/                      # JWT 过滤器
│   ├── dto/                         # 数据传输对象
│   ├── vo/                          # 视图对象
│   └── utils/                       # 工具类
├── src/main/resources/
│   └── application.yml              # 应用配置
├── frontend/
│   ├── src/
│   │   ├── views/                   # 页面组件
│   │   │   ├── Home.vue             # 首页
│   │   │   ├── ArticleDetail.vue    # 文章详情
│   │   │   ├── Search.vue           # 搜索页
│   │   │   ├── Login.vue            # 登录页
│   │   │   ├── Register.vue         # 注册页
│   │   │   ├── Profile.vue          # 个人中心
│   │   │   └── admin/               # 管理后台
│   │   │       ├── Dashboard.vue    # 数据概览
│   │   │       ├── ArticleManage.vue# 文章管理
│   │   │       └── UserManage.vue   # 用户管理
│   │   ├── components/              # 公共组件
│   │   ├── api/                     # API 请求封装
│   │   ├── stores/                  # Pinia 状态
│   │   └── router/                  # 路由配置
│   └── package.json
└── pom.xml
```

---

##  快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.x
- Redis 7.x
- Elasticsearch 8.x

### 1. 克隆项目

```bash
git clone https://github.com/Councillor-Talis/Global-News-System.git
cd global-news-system
```

### 2. 数据库初始化

在 MySQL 中创建数据库并导入初始化 SQL：

```sql
CREATE DATABASE gns_db DEFAULT CHARACTER SET utf8mb4;
```

> 数据库表结构请参考 `src/main/resources/sql/init.sql`（如有）

### 3. 配置后端

编辑 `src/main/resources/application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gns_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: your_mysql_username       # 修改为你的 MySQL 用户名
    password: your_mysql_password       # 修改为你的 MySQL 密码

  elasticsearch:
    uris: http://localhost:9200         # Elasticsearch 地址

  data:
    redis:
      host: localhost                   # Redis 地址
      port: 6379

gns:
  newsapi:
    key: your_newsapi_key               # 填入你的 NewsAPI Key
    base-url: https://newsapi.org/v2

  upload:
    path: /your/path/uploads/avatar/    # 头像上传本地路径
    url-prefix: /uploads/avatar/
```

>  NewsAPI Key 请前往 [https://newsapi.org/](https://newsapi.org/) 免费注册获取。

### 4. 启动后端

```bash
# 使用 Maven Wrapper
./mvnw spring-boot:run

# 或者使用本地 Maven
mvn spring-boot:run
```

后端启动后，API 服务默认运行在 `http://localhost:8080`。

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器默认运行在 `http://localhost:5173`。

---

##  主要功能模块

###  新闻聚合

- **定时采集**：每 15 分钟自动从 NewsAPI 各来源拉取最新新闻
- **正文抓取**：使用 Jsoup 爬取原文页面完整内容，NewsAPI 摘要兜底
- **去重处理**：基于文章 URL 自动去重，避免重复入库
- **智能分类**：多维度关键词匹配，自动将文章归入对应分类

###  搜索功能

- 基于 Elasticsearch 的高效全文检索
- 支持对标题、摘要、正文进行综合搜索
- 管理员可手动触发 ES 索引重建

###  评论系统

- 支持对文章发表评论及多级回复
- 评论点赞 / 取消点赞（已登录用户）
- 登录用户可删除自己的评论，管理员可删除任意评论

###  用户系统

- 注册 / 登录（JWT 无状态认证，有效期 2 小时）
- 个人中心：修改用户名、密码、上传头像
- 角色区分：普通用户（role=0）/ 管理员（role=1）

###  管理后台

| 功能 | 说明 |
|------|------|
| 数据概览 | 文章总数、用户数、今日新增等统计数据 |
| 文章管理 | 列表查询、新增、编辑、删除、上下架 |
| 用户管理 | 列表查询、禁用/启用账号、删除用户 |
| 手动采集 | 立即触发一次新闻采集任务 |
| ES 重建索引 | 一键重建全部文章的 Elasticsearch 索引 |

---

##  API 接口概览

### 公开接口（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/news/list` | 获取新闻列表（分页、分类、来源筛选） |
| GET | `/api/news/detail/{id}` | 获取文章详情 |
| GET | `/api/news/hot` | 获取热点文章（浏览量 Top 5） |
| GET | `/api/news/search` | 全文搜索 |
| GET | `/api/news/related` | 获取相关推荐文章 |
| GET | `/api/category/list` | 获取分类列表 |
| GET | `/api/comment/list` | 获取文章评论列表 |
| GET | `/api/comment/count` | 获取文章评论数 |

### 需要登录

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/comment/add` | 发表评论 / 回复 |
| POST | `/api/comment/like/{id}` | 点赞 / 取消点赞 |
| DELETE | `/api/comment/{id}` | 删除评论 |
| PUT | `/api/user/profile` | 更新个人信息 |
| POST | `/api/user/avatar` | 上传头像 |

### 管理员接口（需 ADMIN 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/stats` | 数据统计 |
| POST | `/api/admin/article` | 新增文章 |
| PUT | `/api/admin/article/{id}` | 编辑文章 |
| DELETE | `/api/admin/article/{id}` | 删除文章 |
| PUT | `/api/admin/article/{id}/status` | 更新文章状态 |
| PUT | `/api/admin/user/{id}/status` | 禁用/启用用户 |
| DELETE | `/api/admin/user/{id}` | 删除用户 |
| POST | `/api/admin/crawler/trigger` | 手动触发采集 |
| POST | `/api/admin/es/reindex` | 重建 ES 索引 |

---

##  配置说明

### 新闻来源配置

新闻来源通过数据库表 `t_source_config` 进行管理，字段说明：

| 字段 | 说明 |
|------|------|
| `source_name` | 来源名称（如 BBC News） |
| `api_url` | 自定义 NewsAPI 请求 URL（优先使用） |
| `category_id` | 默认分类 ID |
| `is_active` | 是否启用（1=启用，0=禁用） |

### 定时采集周期

默认每 **15 分钟**自动采集一次，可修改 `NewsCrawlerService.java` 中的 Cron 表达式：

```java
@Scheduled(cron = "0 0/15 * * * ?")
```

---

##  贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/your-feature`)
3. 提交更改 (`git commit -m 'feat: add some feature'`)
4. 推送到分支 (`git push origin feature/your-feature`)
5. 提交 Pull Request

---

##  许可证

本项目基于 [MIT License](LICENSE) 开源。

---

<div align="center">

Made with  by [Councillor-Talis](https://github.com/Councillor-Talis)

</div>
