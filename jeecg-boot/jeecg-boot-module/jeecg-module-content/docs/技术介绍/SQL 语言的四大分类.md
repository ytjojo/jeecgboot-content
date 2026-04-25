

### 1. 核心概念：SQL 语言的四大分类

要理解 DDL，最好先了解 SQL 语言的整体分类。SQL 通常被分为四类：

1.  **DDL - 数据定义语言**
2.  **DML - 数据操作语言**
3.  **DCL - 数据控制语言**
4.  **TCL - 事务控制语言**

它们各自负责不同的任务，共同协作来管理和操作数据库。

---

### 2. DDL - 数据定义语言

**核心作用**：定义和管理数据库的结构和模式。简单来说，就是**创建、修改、删除数据库中的各种“对象”**，但不直接处理对象里面的数据。

**主要命令**：

*   **`CREATE`**：创建新的数据库对象。
    *   `CREATE DATABASE my_db;` （创建数据库）
    *   `CREATE TABLE users (id INT, name VARCHAR(100));` （创建表）
    *   `CREATE INDEX idx_name ON users (name);` （创建索引）
    *   `CREATE VIEW user_view AS SELECT id, name FROM users;` （创建视图）

*   **`ALTER`**：修改已存在的数据库对象的结构。
    *   `ALTER TABLE users ADD COLUMN email VARCHAR(255);` （增加列）
    *   `ALTER TABLE users DROP COLUMN email;` （删除列）
    *   `ALTER TABLE users MODIFY COLUMN name VARCHAR(200);` （修改列数据类型）

*   **`DROP`**：删除整个数据库对象。
    *   `DROP TABLE users;` （删除表）
    *   `DROP DATABASE my_db;` （删除数据库）
    *   `DROP INDEX idx_name;` （删除索引）

*   **`TRUNCATE`**：清空表中的所有数据，但保留表的结构。它比 `DELETE` 更快，因为它不记录每一行的删除操作，而是直接释放数据页。
    *   `TRUNCATE TABLE users;`

**特点**：
*   DDL 语句通常是**隐式提交**的。一旦执行成功，更改会立即永久生效，无法回滚（在大多数数据库系统中，如 MySQL、Oracle。但 PostgreSQL 等支持 DDL 事务回滚）。
*   操作对象是数据库的结构，而不是具体的数据行。

---

### 3. 其他相关名词详解

#### DML - 数据操作语言

**核心作用**：**对数据库表中的数据进行增、删、改、查**。这是我们最常使用的部分。

**主要命令**：

*   **`SELECT`**：从表中查询数据。 (**虽然只是读操作，但通常被归为DML**)
*   **`INSERT`**：向表中插入新数据。
    *   `INSERT INTO users (id, name) VALUES (1, 'Alice');`
*   **`UPDATE`**：更新表中已存在的数据。
    *   `UPDATE users SET name = 'Bob' WHERE id = 1;`
*   **`DELETE`**：从表中删除数据。
    *   `DELETE FROM users WHERE id = 1;`

**特点**：
*   DML 操作**不会自动提交**，可以通过事务控制进行回滚（`ROLLBACK`）。
*   操作对象是表中的数据行。

#### DCL - 数据控制语言

**核心作用**：控制对数据库的访问权限和安全性。

**主要命令**：

*   **`GRANT`**：授予用户或角色访问权限。
    *   `GRANT SELECT, INSERT ON users TO user1;`
*   **`REVOKE`**：撤销用户或角色的访问权限。
    *   `REVOKE INSERT ON users FROM user1;`

#### TCL - 事务控制语言

**核心作用**：管理数据库中的事务，确保数据的完整性和一致性。

**主要命令**：

*   **`COMMIT`**：提交事务，使所有自上次提交后的修改成为永久性的。
*   **`ROLLBACK`**：回滚事务，撤销所有未提交的修改。
*   **`SAVEPOINT`**：在事务中设置一个保存点，可以回滚到该点而不是整个事务。

---

### 总结与对比

为了更清晰地理解，这里有一个简单的对比表格：

| 类别 | 英文全称 | 中文含义 | 核心命令 | 操作对象 | 是否自动提交 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **DDL** | Data **Definition** Language | 数据**定义**语言 | `CREATE`, `ALTER`, `DROP`, `TRUNCATE` | 数据库、表、索引等**结构** | **是** (通常) |
| **DML** | Data **Manipulation** Language | 数据**操作**语言 | `SELECT`, `INSERT`, `UPDATE`, `DELETE` | 表中的**数据行** | **否** |
| **DCL** | Data **Control** Language | 数据**控制**语言 | `GRANT`, `REVOKE` | 访问**权限** | 依情况而定 |
| **TCL** | Transaction **Control** Language | 事务**控制**语言 | `COMMIT`, `ROLLBACK`, `SAVEPOINT` | **事务** | - |

**简单记忆**：
*   **DDL**：管“房子”的结构（盖房、拆墙、拆房）。
*   **DML**：管“房子”里的“东西”（增删改查家具）。
*   **DCL**：管“钥匙”和“权限”（谁能进哪个房间）。
*   **TCL**：管“所有操作”的打包（要么全部成功，要么全部失败恢复原样）。