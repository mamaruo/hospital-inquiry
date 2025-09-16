# 在线问诊项目代码规范


# I. 核心原则

*   **清晰性**：代码首先是写给人看的，其次才是给机器执行的。
*   **一致性**：整个项目的代码风格、命名和结构保持统一。
*   **简洁性**：如无必要，勿增实体 (KISS - Keep It Simple, Stupid)。

# II. Java (后端) 规范

**基础**：以 **《阿里巴巴Java开发手册》** 为总纲。这里只强调项目中最常用到的几点。

**1. 命名规范**
*   **包名**：全部小写，点分隔。格式：`com.公司域.项目名.模块名`。
    *   示例：`com.clinic.consultation.controller`, `com.clinic.consultation.service`, `com.clinic.consultation.model`
*   **类名**：大驼峰 (UpperCamelCase)。
    *   示例：`ChatController`, `WebSocketConfig`, `ChatMessage`
*   **方法名/变量名**：小驼峰 (lowerCamelCase)。
    *   示例：`sendMessage`, `stompClient`, `consultationId`
*   **常量名**：全大写，下划线分隔 (UPPER_SNAKE_CASE)。
    *   示例：`MAX_MESSAGE_LENGTH`
*   **领域模型后缀**：
    *   **Controller** 方法接收的请求数据：`...Request` 或 `...DTO` (Data Transfer Object)。
    *   **Service** 返回给 Controller 的数据：`...VO` (View Object)。
    *   **数据库实体**：`...DO` (Data Object) 或直接用实体名，如 `User`, `Message`。

**2. Controller/Service/Repository(DAO) 层职责**
*   **Controller 层**：
    *   负责接收和校验前端参数。
    *   调用 Service 层处理业务。
    *   处理HTTP相关逻辑（如设置响应头、状态码）。
    *   **禁止**编写任何业务逻辑。
*   **Service 层**：
    *   核心业务逻辑层。
    *   处理复杂的业务流程，可以调用其他 Service 或 Repository。
    *   进行事务管理。
*   **Repository/DAO 层**：
    *   只负责与数据库交互（增删改查）。
    *   **禁止**包含任何业务逻辑。

**3. 异常处理**
*   使用全局异常处理器 (`@ControllerAdvice`) 统一处理未捕获的异常。
*   对于可预期的业务异常（如“用户不存在”），应定义自定义异常类（如 `UserNotFoundException`）。
*   返回给前端的错误信息应包含明确的错误码和错误信息。

**4. 注释**
*   所有公开的类、方法、常量都必须有 Javadoc 注释。
*   复杂或非直观的业务逻辑代码块前，必须有单行或多行注释解释其目的（“为什么这么做”），而不是解释代码本身（“代码做了什么”）。

## III. Vue (前端) 规范

**基础**：遵循 **Vue 官方风格指南 (A类和B类规则必须遵守)**。

**1. 命名规范**
*   **组件文件名**：大驼峰 (PascalCase)。必须是多个单词。
    *   **正确**：`ConsultationWindow.vue`, `MessageItem.vue`
    *   **错误**：`Chat.vue` (应为 `ChatRoom.vue` 或其他)
*   **Props**：在 JS/TS 中使用小驼峰 (camelCase)，在模板中使用短横线分隔 (kebab-case)。
    *   JS: `props: { consultationId: String }`
    *   Template: `<MessageItem :consultation-id="currentId" />`
*   **事件**：同 Props，JS 中 `this.$emit('sendMessage')`，模板中 `@send-message="handleMessage"`。

**2. 文件结构**
推荐按功能模块组织文件，而不是按文件类型。
```
src/
├── api/              # API 请求模块
│   └── consultation.js
├── components/       # 全局通用组件
│   └── BaseButton.vue
├── views/ (或 pages/) # 页面级组件
│   ├── Consultation/
│   │   ├── components/       # 此页面专属的子组件
│   │   │   └── MessageBubble.vue
│   │   └── ConsultationView.vue # 页面主组件
│   └── LoginView.vue
├── store/            # 状态管理 (Pinia)
│   └── consultation.js
└── ...
```

**3. API 管理**
*   所有后端 API 请求必须统一封装在 `/src/api` 目录下，按模块划分。
*   组件内不应直接出现 `axios.get('/api/...')` 这样的硬编码。
*   示例 (`/src/api/consultation.js`):
    ```javascript
    import request from '@/utils/request'; // 封装好的axios实例

    export function getHistoryMessages(consultationId) {
        return request({
            url: `/consultations/${consultationId}/messages`,
            method: 'get'
        });
    }
    ```

## IV. Git 提交规范

使用 **Conventional Commits** 规范，这对于生成变更日志和快速定位代码变更非常有用。

**格式**: `type(scope): subject`

*   **type**:
    *   `feat`: 新功能
    *   `fix`: 修复 bug
    *   `docs`: 只修改了文档
    *   `style`: 代码格式修改（不影响逻辑）
    *   `refactor`: 代码重构
    *   `test`: 添加或修改测试
    *   `chore`: 构建过程或辅助工具的变动
*   **scope**: (可选) 本次提交影响的范围，如 `chat`, `auth`, `user`
*   **subject**: 简短的提交描述

**示例**:
*   `feat(chat): implement real-time messaging with WebSocket`
*   `fix(auth): resolve token expiration issue on page refresh`
*   `docs(readme): update project setup instructions`
