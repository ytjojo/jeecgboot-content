Mybatis的多表查询操作 


# 2.1 前言
------

表之间的关系有几种：一对多、多对一、 一对一、多对多

在多对一关系中，把多的部分拆成一个一个对象其实就是一对一关系，如账户和用户是多对一关系，但每个账户只对应一个用户。所以在mybatis中，多对一的关系可以看成一对一的关系。

这里我把一对多和多对一的xml配置方式总结了一下，同时还有加载方式。

一对多，多对多：通常情况下我们都是采用延迟加载。

多对一，一对一：通常情况下我们都是采用立即加载。

至于注解方式和多对多查询的xml和注解方式我会另外写博客。

# 2.2 数据库表及关系
-----------

我们以用户和账户为例，用户可以有多个账户，账户只能对应一个用户。所以用户对账户是一对多关系，账户对用户是多对一关系。表如下图所示，用户表user，账户表account，账户表UID对应用户表id。

 ![](https://ucc.alicdn.com/pic/developer-ecology/pbjttotxrbkzo_3a38a0a2618c4847a36a3844cacd9971.png?x-oss-process=image%2Fresize%2Cw_1400%2Cm_lfit%2Fformat%2Cwebp) 

 ![](https://ucc.alicdn.com/pic/developer-ecology/pbjttotxrbkzo_47d43f9e92da4112996a145376b0f0f7.png?x-oss-process=image%2Fresize%2Cw_1400%2Cm_lfit%2Fformat%2Cwebp) 

# 2.3 一对多查询
---------

首先我们要在User实体类中添加List accounts的集合成员变量，表示一对多映射关系，主表实体含有从表实体的集合引用。

```java
public class User implements Serializable {
    private Integer id;
    private String username;
    private String address;
    private String sex;
    private Date birthday;

    // 一对多映射关系，主表实体含有从表实体的集合引用
    private List<Account> accounts;

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", address='" + address + '\'' +
                ", sex='" + sex + '\'' +
                ", birthday=" + birthday +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
}
```



同时在User Dao接口中提供查询所有方法findAll，在Account Dao接口中提供根据id查询user的方法findById，以便延时加载时调用。

这里说明因为用户可能对应许多账户，当我们查询用户时可能并不需要账户信息，而且如果我们每次查询用户时都立即查询用户的账户信息，并且账户信息有很多，势必对内存有很大的开销。所以当我们需要账户信息时再调用findById方法去查询用户对应的账户信息。

```java
public interface IUserDao {
    /**
     * 查询所有操作,并携带账户信息
     * @return
     */
    List<User> findAll();

    /**
     * 根据id查询一个用户
     * @param uid
     */
    User findById(Integer uid);
}

public interface IAccountDao {
    /**
     * 查询所有账户
     * @return
     */
    List<Account> findAll();

    /**
     * 根据用户id查询账户
     * @param uid
     * @return
     */
    List<Account> findByUid(Integer uid);
}
```


然后配置userDao.xml，说明会在代码中给出。

```xml
<mapper namespace="com.cc.dao.IUserDao">
    <!-- 定义resultMap -->
    <!-- 因为在主配置文件中配置了domain包下的所有实体类别名，所以这里封装类型只需要写实体类名即可，不分大小写 -->
    <resultMap id="userWithAccount" type="user">
        <!-- 封装user对象 -->
        <id property="id" column="id"></id>
        <result property="username" column="username"></result>
        <result property="address" column="address"></result>
        <result property="sex" column="sex"></result>
        <result property="birthday" column="birthday"></result>
        <!-- 配置user对象中account集合的映射 -->
        <!-- 定义一对多的关系映射，实现对account的封装，用collection标签
             ofType属性指定内容：要封装的实体对象类型
             select属性指定内容：查询用户的唯一标识
             column属性指定内容：用户根据id查询是所需要的参数 -->
        <collection property="accounts" ofType="account" column="id" 
                    select="com.cc.dao.IAccountDao.findByUid">
        </collection>
    </resultMap>
    
    <!-- 查询所有 -->
    <select id="findAll" resultMap="userWithAccount">
        select * from user
    </select>
    
    <!-- 根据id查询一个用户 -->
    <select id="findById" parameterType="java.lang.Integer" resultType="user">
        select * from user where id=#{uid};
    </select>
</mapper>
```



当然我们还要在主配置文件中开启延时加载，默认情况下是立即加载。

lazyLoadingEnabled：是否启用延迟加载，mybatis默认为false，不启用延迟加载。lazyLoadingEnabled属性控制全局是否使用延迟加载，特殊关联关系也可以通过嵌套查询中fetchType属性单独配置（fetchType属性值lazy或者eager）。

也就是说我们可以不用在主配置文件中配置而在userDao.xml中配置，这里我们采用全局配置。

```xml
<!-- 配置参数 -->
<settings>
    <!-- 开启Mybatis支持延时加载 -->
    <setting name="lazyLoadingEnabled" value="true"/>
    <setting name="aggressiveLazyLoading" value="false"></setting>
</settings>

<!-- 配置domain包下所有实体类别名 -->
<typeAliases>
    <!-- <typeAlias type="com.cc.domain.User" alias="user"></typeAlias> -->
    <package name="com.cc.domain"></package>
</typeAliases>
```



然后我们就可以测试了

```java
public class UserTest {
    private InputStream in;
    private SqlSessionFactory factory;
    private SqlSession sqlSession;
    private IUserDao userDao;
    
    @Before // 在测试方法执行之前执行
    public void init() throws IOException {
        // 1.读取配置文件，生成字节输入流
        in = Resources.getResourceAsStream("SqlMapConfig.xml");
        // 2.生成SqlSessionFactory
        factory = new SqlSessionFactoryBuilder().build(in);
        // 3.获取SqlSession
        sqlSession = factory.openSession();
        // 4.获取dao的代理对象
        userDao = sqlSession.getMapper(IUserDao.class);
    }
    
    @After // 在测试方法执行之后执行
    public void destory() throws IOException {
        // 提交事务
        sqlSession.commit();
        // 关闭资源
        sqlSession.close();
        in.close();
    }

    /**
     * 测试查询所有账户
     */
    @Test
    public void TestFindAll() {
        // 5.执行查询所有方法
        List<User> userList = userDao.findAll();
        for (User user : userList) {
            System.out.println(user);
            System.out.println(user.getAccounts());
        }
    }
}
```



先把遍历输出部分代码注释掉，测试可以看出我们只查询了用户信息。

 ![](https://ucc.alicdn.com/pic/developer-ecology/pbjttotxrbkzo_92ee2c6055c7495ba7d891560c5e613d.png?x-oss-process=image%2Fresize%2Cw_1400%2Cm_lfit%2Fformat%2Cwebp) 然后去掉注释，发现当我们需要输出用户账户时，他就会去查询用户的账户信息。

 ![](https://ucc.alicdn.com/pic/developer-ecology/pbjttotxrbkzo_8a55f6799ff94f9880c34ead56059ceb.png?x-oss-process=image%2Fresize%2Cw_1400%2Cm_lfit%2Fformat%2Cwebp) 

2.4 多对一及一对一查询
-------------

步骤其实和一对多差不多。

首先我们在account实体类中加入user成员变量表示一对一映射。

```java
public class Account implements Serializable {
    private Integer id;
    private Integer uid;
    private Double money;
    
    // 从表实体应该包含一个主表实体的对象引用
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Double getMoney() {
        return money;
    }

    public void setMoney(Double money) {
        this.money = money;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", uid=" + uid +
                ", money=" + money +
                '}';
    }
}
```



Dao接口中需要的的方法在上面总结一对多查询时的图中已经给出。

然后配置accountDao.xml，这里是立即查询，在我们已经配置全局延时加载的情况下，我们需要配置fetchType=“eager”。

```xml
<mapper namespace="com.cc.dao.IAccountDao">
    <!-- 开启account支持二级缓存 -->
    <cache/>
    
    <!-- 定义封装account和user的resultMap -->
    <resultMap id="accountAndUser" type="account">
        <id property="id" column="aid"></id>
        <result property="uid" column="uid"></result>
        <result property="money" column="money"></result>
        <!-- 定义一对一的关系映射，实现对user的封装
             select属性指定内容：查询用户的唯一标识
             column属性指定内容：用户根据id查询是所需要的参数
             fetchType属性指定内容：lazy延时加载，eager立即加载。 -->
        <association property="user" column="uid" javaType="user" 
                     select="com.cc.dao.IUserDao.findById" fetchType="eager">
        </association>
    </resultMap>
    
    <!-- 查询所有 -->
    <select id="findAll" resultMap="accountAndUser">
        SELECT * from account
    </select>
    
    <!-- 根据用户id查询 -->
    <select id="findByUid" parameterType="java.lang.Integer" 
            resultType="account" useCache="true">
        select * from account where uid = #{uid}
    </select>
</mapper>
```



然后我们就可以测试。可以看出当查询账户时就立即查询了对应的用户信息。

 ![](https://ucc.alicdn.com/pic/developer-ecology/pbjttotxrbkzo_688c983de5e2403190261bb93313f82b.png?x-oss-process=image%2Fresize%2Cw_1400%2Cm_lfit%2Fformat%2Cwebp) 

注意:

 ![](https://ucc.alicdn.com/pic/developer-ecology/pbjttotxrbkzo_7034e3a561d04a1faaa8d0d557a89b8a.png?x-oss-process=image%2Fresize%2Cw_1400%2Cm_lfit%2Fformat%2Cwebp) 

3 使用注解多表查询
==========

3.1 一对一
-------

### 3.1.1 表关系

一对一关系中，表关系由任意一方维护，以人和身份证为例，一个人对应一个身份证，一个身份证对应一个人。本案例中由身份证表维护表关系

 ![](https://ucc.alicdn.com/pic/developer-ecology/pbjttotxrbkzo_bce89b39fa7d45ce943ebf7fb5f4c379.png?x-oss-process=image%2Fresize%2Cw_1400%2Cm_lfit%2Fformat%2Cwebp) 

### 3.1.2 需求

查询所有的身份证，并且要将身份证对应的人也查询出来

### 3.1.3 实现步骤

#### 第一步:建表

```sql
DROP TABLE IF EXISTS `person`;
CREATE TABLE `person` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(30) NOT NULL COMMENT '姓名',
    `age` int(11) NOT NULL COMMENT '年龄',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='用户表';

insert into `person`(`id`,`name`,`age`) values 
(1,'陈乔恩',39),
(2,'钟汉良',44),
(3,'林志颖',44),
(4,'吴奇隆',48);

DROP TABLE IF EXISTS `card`;
CREATE TABLE `card` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `number` varchar(30) NOT NULL COMMENT '身份证号',
    `pid` int(11) NOT NULL COMMENT '所属用户',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='身份证表';

insert into `card`(`id`,`number`,`pid`) values 
(1,'110101199003079008',1),
(2,'110101199003079331',2),
(3,'11010119900307299X',3),
(4,'110101199003070791',4);
```



#### 第二步:创建实体类

> Person 人类

```java
package cn.oldlu.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person {
    private Integer id;
    private String name;
    private Integer age;
}
```



Card 身份证类，因为需求是查询身份证，并且查询对应的人，所以应该是Card实体类中存放Perosn属性

```java
package cn.oldlu.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    private Integer id;
    private String number;
    private Person person;
}
```



#### 第三步:创建dao接口(核心)

> 思考 使用2条SQL语句,查询id=1的身份证信息以及对应的人怎么查

```sql
-- 第一步 查询id=1的身份证信息，找到对应的pid,值为1
SELECT * FROM card WHERE id = 1;

-- 第二步 根据pid的值查询对应的人
SELECT * FROM person WHERE id=1;
```



 ![](https://ucc.alicdn.com/pic/developer-ecology/pbjttotxrbkzo_41791fde30304a9a9d767442546b717a.png?x-oss-process=image%2Fresize%2Cw_1400%2Cm_lfit%2Fformat%2Cwebp) 

> 接下来通过编写两个Dao分别完成以上两个查询语句
> 
> 1.Person实体类比较简单，所以先编写PersonDao中的代码,根据id查询详细信息

```java
package cn.oldlu.dao;

import cn.oldlu.domain.Person;
import org.apache.ibatis.annotations.Select;

public interface PersonDao {
    /*#{id}对应的是形参Integer的值,名字可以任意*/
    @Select("select * from person where id = #{id}")
    Person findById(Integer id);
}
```



2.编写CardDao

```java
package cn.oldlu.dao;

import cn.oldlu.domain.Card;
import cn.oldlu.domain.Person;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CardDao {
    
    @Select("select * from card")
    @Results({
        @Result(column = "id", property = "id"), //将结果集中的id字段的值赋值给id属性
        @Result(column = "number", property = "number"),//将结果集中的number字段的值赋值给number属性
        @Result(
            column = "pid",
            property = "person",
            javaType = Person.class,
            one = @One(select = "cn.oldlu.dao.PersonDao.findById")
        ),//首先结果集中的pid字段的值传给findeById方法，然后将查询结果封装成Person类的对象，最后赋值给person属性
    })
    List<Card> findAll();
}
```



#### 第四步:编写测试方法

```java
@Test
public void testFindAll() throws Exception {
    /*1.读取配置文件*/
    InputStream is = Resources.getResourceAsStream("mybatis-config.xml");
    /*2.解析配置文件*/
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    /*3.获取操作数据库的对象*/
    SqlSession sqlSession = sqlSessionFactory.openSession();
    /*4.获取代理对象*/
    CardDao cardDao = sqlSession.getMapper(CardDao.class);
    /*5.调用代理对象的方法完成查询*/
    List<Card> cards = cardDao.findAll();
    /*6.释放资源*/
    sqlSession.close();

    //测试查询结果
    System.out.println(cards);
}
```



3.2 一对多
-------

### 3.2.1 表关系

一对多关系中，表关系由多的一方维护，以班级和学生为例，一个班级可以有多个学生，学生表应该维护表关系。

 ![](https://ucc.alicdn.com/pic/developer-ecology/pbjttotxrbkzo_78ae4fa36a0c4b99b23838bc69be7bb9.png?x-oss-process=image%2Fresize%2Cw_1400%2Cm_lfit%2Fformat%2Cwebp) 

### 3.2.2 需求

查询所有班级信息，并且要将班级对应的学生信息也同时查询出来

### 3.2.3 实现步骤

#### 第一步:建表

```sql
DROP TABLE IF EXISTS `classes`;
CREATE TABLE `classes` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(20) DEFAULT NULL COMMENT '班级名称',
    PRIMARY KEY (`id`)
) ENGINE=INNODB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='班级表';

INSERT INTO `classes`(`id`,`name`) VALUES 
(1,'一班'),
(2,'二班');

DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(24) NOT NULL,
    `age` INT(11) NOT NULL,
    `cid` INT(11) NOT NULL COMMENT '所属班级',
    PRIMARY KEY (`id`),
    KEY `cid` (`cid`)
) ENGINE=INNODB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;

INSERT INTO `student`(`id`,`name`,`age`,`cid`) VALUES 
(8,'王力宏',23,1),
(9,'张韶涵',22,1),
(10,'张晋',24,1),
(11,'罗晋',25,1),
(12,'唐僧',12,2),
(13,'孙悟空',22,2),
(14,'猪八戒',21,2);
```



#### 第二步:创建实体类

> Student学生类，和上文提到的一样。有就不用创建了

```java
package cn.oldlu.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    private Integer id;
    private String name;
    private Integer age;
}
```



Classes班级类，因为需求是查询班级，以及对应的学生，所以应该是班级类中定义List表示一堆学生

```java
package cn.oldlu.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Classes {
    private Integer id;
    private String name;
    private List<Student> students;
}
```



#### 第三步:创建dao接口(核心)

> 思考 使用2条SQL语句，查询id=1的班级信息以及对应的学生怎么查

```sql
-- 第一步 查询ID=1的班级信息
SELECT * FROM classes WHERE id = 1;

-- 第二步 查询对应的学生信息
SELECT * FROM student WHERE cid = 1;
```



 ![](https://ucc.alicdn.com/pic/developer-ecology/pbjttotxrbkzo_3bde7e90dc29479887e82e39f3a021f5.png?x-oss-process=image%2Fresize%2Cw_1400%2Cm_lfit%2Fformat%2Cwebp) 

> 接下来通过编写两个Dao分别完成以上两个查询语句
> 
> 1.Student实体类比较简单，所以先编写StudentDao中的代码,根据cid查询学生信息

```java
package cn.oldlu.dao;

import cn.oldlu.domain.Student;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface StudentDao {
    /*#{id}对应的是形参Integer的值,名字可以任意*/
    @Select("SELECT * FROM student WHERE cid = #{cid};")
    List<Student> findByCid(Integer cid);
}
```



2.编写ClassesDao

```java
package cn.oldlu.dao;

import cn.oldlu.domain.Classes;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ClassesDao {
    
    @Select("SELECT * FROM classes")
    @Results({
        @Result(column = "id", property = "id"),//将结果集中的id字段的值赋值给id属性
        @Result(column = "name", property = "name"),//将结果集中的name字段的值赋值给name属性
        @Result(
            column = "id",
            property = "students",
            javaType = List.class,
            many = @Many(select = "cn.oldlu.dao.StudentDao.findByCid")
        )//首先结果集中的id字段的值传给findByCid方法，然后将查询结果封装成List类的对象，最后赋值给students属性
    })
    List<Classes> findAll();
}
```



#### 第四步:编写测试方法

```java
@Test
public void testFindAll() throws Exception {
    /*1.读取配置文件*/
    InputStream is = Resources.getResourceAsStream("mybatis-config.xml");
    /*2.解析配置文件*/
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    /*3.获取操作数据库的对象*/
    SqlSession sqlSession = sqlSessionFactory.openSession();
    /*4.获取代理对象*/
    ClassesDao classesDao = sqlSession.getMapper(ClassesDao.class);
    /*5.调用代理对象的方法完成查询*/
    List<Classes> classes = classesDao.findAll();
    /*6.释放资源*/
    sqlSession.close();

    //测试查询结果
    System.out.println(classes);
}
```



## 3.3 多对多
-------

### 3.3.1 表关系

多对多关系中表关系由中间表维护，以学生和课程为例，一个学生对应多们课程，一个课程同时对应多个学生

 ![](https://ucc.alicdn.com/pic/developer-ecology/pbjttotxrbkzo_02d3165e9c0846689580c00c5de48767.png?x-oss-process=image%2Fresize%2Cw_1400%2Cm_lfit%2Fformat%2Cwebp) 

### 3.3.2 需求

查询所有的学生，并且要将学生对应的课程也查询出来

### 3.3.3 实现步骤

#### 第一步: 建表

```sql
DROP TABLE IF EXISTS `classes`;
CREATE TABLE `classes` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(20) DEFAULT NULL COMMENT '班级名称',
    PRIMARY KEY (`id`)
) ENGINE=INNODB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='班级表';

INSERT INTO `classes`(`id`,`name`) VALUES 
(1,'一班'),
(2,'二班');

DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(24) NOT NULL,
    `age` INT(11) NOT NULL,
    `cid` INT(11) NOT NULL COMMENT '所属班级',
    PRIMARY KEY (`id`),
    KEY `cid` (`cid`)
) ENGINE=INNODB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;

INSERT INTO `student`(`id`,`name`,`age`,`cid`) VALUES 
(8,'王力宏',23,1),
(9,'张韶涵',22,1),
(10,'张晋',24,1),
(11,'罗晋',25,1),
(12,'唐僧',12,2),
(13,'孙悟空',22,2),
(14,'猪八戒',21,2);

DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(30) NOT NULL COMMENT '课程名',
    PRIMARY KEY (`id`)
) ENGINE=INNODB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='课程表';

INSERT INTO `course`(`id`,`name`) VALUES 
(1,'语文'),
(2,'数学');

DROP TABLE IF EXISTS `stu_cr`;
CREATE TABLE `stu_cr` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `sid` INT(11) NOT NULL,
    `cid` INT(11) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=INNODB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

INSERT INTO `stu_cr`(`id`,`sid`,`cid`) VALUES 
(1,8,1),
(2,8,2),
(3,9,1),
(4,10,2),
(5,13,2),
(6,13,1),
(7,14,1),
(8,11,2),
(9,12,1),
(10,12,2);
```



#### 第二步:创建实体类

> Course 课程类

```java
package cn.oldlu.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Course {
    /**课程ID*/
    private Integer id;
    /**课程名*/
    private String name;
}
```



Student 学生类,学生类之前已经定义过了，只需要添加新的course属性即可。因为需求是查询学生信息，以及对应的课程，所以应该在学生类中定义List，表示一堆课程

```java
package cn.oldlu.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    private Integer id;
    private String name;
    private Integer age;
    private List<Course> courses;
}
```



#### 第三步:创建Dao接口(核心)

> 思考，使用2条SQL如何查询id=8的学生的信息以及对应的课程信息

Sql

```sql
-- 第一步 查询id=8的学生信息
SELECT * FROM student WHERE id = 8;

-- 第二步 根据学生的id查询对应的课程信息
SELECT * FROM course c 
INNER JOIN stu_cr sc ON c.id = sc.cid AND sc.sid=8;
```



 ![](https://ucc.alicdn.com/pic/developer-ecology/pbjttotxrbkzo_64b7f7d759694c7da663085e948fd2ce.png?x-oss-process=image%2Fresize%2Cw_1400%2Cm_lfit%2Fformat%2Cwebp) 

> 接下来通过编写两个Dao分别完成以上两个查询语句
> 
> 1.Course实体类比较简单，所以先编写CourseDao中的代码,根据sid查询课程信息

```java
package cn.oldlu.dao;

import cn.oldlu.domain.Course;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CourseDao {
    /*#{sid}对应的是形参Integer的值,名字可以任意*/
    @Select("SELECT * FROM course c INNER JOIN stu_cr sc ON c.id = sc.cid AND sc.sid=#{sid};")
    List<Course> findBySid(Integer sid);
}
```



2.编写StudentDao

```java
package cn.oldlu.dao;

import cn.oldlu.domain.Student;
import org.apache.ibatis.annotations.*;
import java.util.List;

public interface StudentDao {
    @Select("select * from student")
    @Results({
        @Result(column = "id", property = "id"),//将结果集中的id字段的值赋值给id属性
        @Result(column = "name", property = "name"),//将结果集中的name字段的值赋值给name属性
        @Result(column = "age", property = "age"),//将结果集中的age字段的值赋值age属性
        @Result(
            column = "id",
            property = "courses",
            javaType = List.class,
            many = @Many(select = "cn.oldlu.dao.CourseDao.findBySid")
        )//首先结果集中的id字段的值传给findBySid方法，然后将查询结果封装成List类的对象，最后赋值给courses属性
    })
    List<Student> findAll();
}
```



#### 第四步:编写测试类

```java
@Test
public void testFindAll() throws Exception {
    /*1.读取配置文件*/
    InputStream is = Resources.getResourceAsStream("mybatis-config.xml");
    /*2.解析配置文件*/
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
    /*3.获取操作数据库的对象*/
    SqlSession sqlSession = sqlSessionFactory.openSession();
    /*4.获取代理对象*/
    StudentDao studentDao = sqlSession.getMapper(StudentDao.class);
    /*5.调用代理对象的方法完成查询*/
    List<Student> students = studentDao.findAll();
    /*6.释放资源*/
    sqlSession.close();

    //测试查询结果
    System.out.println(students);
}
```

