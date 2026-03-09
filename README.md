# 设备租赁系统

采用 SpringBoot 3 + Vue 3 + MySQL 构建的完整设备租赁管理系统，支持多角色用户和完整的业务流程。

## 📋 项目概述

这是一个功能完整的设备租赁系统，支持用户注册、设备浏览、订单管理、评价反馈等功能。系统采用前后端分离架构，包含普通用户、设备管理员和系统管理员三种角色。

## 🛠️ 技术栈

### 后端技术
- **Spring Boot 3.1.2** - 基础框架
- **MyBatis-Plus 3.5.3.1** - 持久层框架
- **Spring Security** - 权限校验框架
- **Redis** - 缓存和限流
- **RabbitMQ** - 消息队列（邮件发送）
- **JWT (java-jwt 4.3.0)** - 令牌生成和校验
- **MySQL 8.0** - 数据库
- **Swagger** - API 文档生成
- **FastJSON2** - JSON 处理

### 前端技术
- **Vue 3.3.4** - 前端框架
- **Vue Router 4.2.4** - 路由管理
- **Axios 1.4.0** - HTTP 请求
- **Element Plus 2.3.9** - UI 组件库
- **VueUse 10.3.0** - 工具库
- **Vite 4.4.6** - 构建工具
- **Pinia** - 状态管理

## 👥 用户角色

### 1. 普通用户 (user)
- 用户注册与登录
- 设备浏览与搜索
- 设备租赁与订单管理
- 租赁评价与反馈
- 个人信息管理

### 2. 设备管理员 (device_admin)
- 设备信息管理（上架、下架、编辑）
- 租赁订单处理
- 设备状态监控
- 租赁统计与报表生成

### 3. 系统管理员 (system_admin)
- 用户管理
- 权限分配
- 系统设置
- 日志管理
- 数据备份与恢复
- 仪表盘数据统计

## 📦 核心功能

### 后端功能
- ✅ 用户注册（邮箱验证码）
- ✅ 用户登录（JWT 令牌）
- ✅ 密码重置（邮箱验证）
- ✅ 设备管理（CRUD、状态控制）
- ✅ 订单管理（创建、支付、取消、完成、归还）
- ✅ 评价系统（添加、删除、查询）
- ✅ 用户管理（角色分配、状态控制）
- ✅ 系统日志（记录、查询、清理）
- ✅ 系统设置（配置管理）
- ✅ 数据备份（备份记录管理）
- ✅ 仪表盘统计（数据汇总）
- ✅ 邮件发送（RabbitMQ 异步队列）
- ✅ IP 限流（防刷接口）
- ✅ 跨域处理（过滤器）
- ✅ 请求日志（雪花 ID 追踪）
- ✅ 接口文档（Swagger 自动生成）
- ✅ 多环境配置（开发/生产）
- ✅ 定时任务（日志清理）

### 前端功能
- ✅ 登录页面
- ✅ 注册页面
- ✅ 忘记密码页面
- ✅ 用户仪表盘
- ✅ 设备列表与搜索
- ✅ 设备详情与租赁
- ✅ 我的订单管理
- ✅ 订单评价功能
- ✅ 个人信息管理
- ✅ 设备管理（设备管理员）
- ✅ 订单管理（设备管理员）
- ✅ 用户管理（系统管理员）
- ✅ 系统管理（系统管理员）
- ✅ 路由守卫（权限控制）

启动方式

#### 第一步：启动基础服务

##### 1.1 启动 MySQL

```bash
# Windows: 打开服务管理器启动 MySQL 服务，或使用命令
net start mysql

# 创建数据库并导入表结构
mysql -u root -p < database.sql
```

##### 1.2 启动 Redis

```bash
# Windows: 下载 Redis for Windows 或使用 WSL
redis-server
redis-cli ping  # 应返回 PONG
```

##### 1.3 启动 RabbitMQ

```bash
# Windows: 安装 RabbitMQ 后，以管理员身份运行命令
rabbitmq-service.bat start

# 管理界面访问地址：http://localhost:15672
# 默认用户名和密码：guest/guest
```

#### 第二步：配置后端项目

```bash
cd d:/zulin/my-project-backend

# 编辑 src/main/resources/application-dev.yml
# 根据实际情况修改配置
```

#### 第三步：启动后端项目

```bash
# 方式一：使用启动脚本（推荐）
cd d:/zulin/my-project-backend
启动后端.bat

# 方式二：使用 Maven 命令启动
cd d:/zulin/my-project-backend
mvn spring-boot:run

# 验证后端启动成功
# 访问 Swagger 文档：http://localhost:8080/swagger-ui/index.html
```

#### 第四步：安装前端依赖

```bash
cd d:/zulin/my-project-frontend
npm install

# 如果遇到网络问题，可以使用国内镜像
npm install --registry=https://registry.npmmirror.com
```

#### 第五步：启动前端项目

```bash
# 方式一：使用启动脚本（推荐）
cd d:/zulin/my-project-frontend
启动前端.bat

# 方式二：使用 npm 命令
cd d:/zulin/my-project-frontend
npm run dev

# 验证前端启动成功
# 访问 http://localhost:5173
```

## 📁 项目结构

```
d:/zulin/
├── database.sql                 # 数据库初始化脚本
├── README.md                    # 项目说明文档
├── my-project-backend/          # 后端项目
│   ├── src/main/java/com/example/
│   │   ├── config/              # 配置类（安全、Swagger、RabbitMQ）
│   │   ├── controller/          # 控制器
│   │   │   ├── EquipmentController.java
│   │   │   ├── OrderController.java
│   │   │   ├── EvaluationController.java
│   │   │   ├── UserController.java
│   │   │   ├── SystemController.java
│   │   │   └── AuthorizeController.java
│   │   ├── entity/              # 实体类
│   │   │   ├── Equipment.java
│   │   │   ├── Order.java
│   │   │   ├── Evaluation.java
│   │   │   ├── SystemLog.java
│   │   │   ├── SystemSettings.java
│   │   │   └── Backup.java
│   │   ├── mapper/              # MyBatis Mapper
│   │   ├── service/             # 业务逻辑
│   │   ├── filter/              # 过滤器（JWT、CORS、日志）
│   │   ├── listener/            # 消息队列监听器
│   │   └── utils/               # 工具类
│   │   └── resources/
│   │       ├── mapper/          # MyBatis XML 映射文件
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── pom.xml
└── my-project-frontend/         # 前端项目
    ├── src/
    │   ├── net/                 # 网络请求封装
    │   ├── router/              # 路由配置
    │   ├── stores/              # Pinia 状态管理
    │   ├── views/               # 页面组件
    │   │   ├── UserDashboard.vue
    │   │   └── dashboard/
    │   ├── App.vue
    │   └── main.js
    ├── public/
    ├── vite.config.js
    └── package.json
```

## 🔐 默认测试账号

系统已预置以下测试账号（密码均为 `123456`）：

- **系统管理员**
  - 用户名：admin
  - 邮箱：admin@example.com
  - 角色：system_admin

- **设备管理员**
  - 用户名：device_admin
  - 邮箱：device_admin@example.com
  - 角色：device_admin

- **普通用户**
  - 用户名：user
  - 邮箱：user@example.com
  - 角色：user

## 📊 数据库设计

系统包含以下核心数据表：

1. **db_account** - 用户账户表
2. **db_equipment** - 设备信息表
3. **db_order** - 租赁订单表
4. **db_evaluation** - 设备评价表
5. **db_system_log** - 系统日志表
6. **db_system_settings** - 系统设置表
7. **db_backup** - 数据备份表

## 🔧 API 文档

启动后端服务后，访问 Swagger 文档查看完整 API 接口：

```
http://localhost:8080/swagger-ui/index.html
```

## 📄 许可证

本项目仅供学习和参考使用。
