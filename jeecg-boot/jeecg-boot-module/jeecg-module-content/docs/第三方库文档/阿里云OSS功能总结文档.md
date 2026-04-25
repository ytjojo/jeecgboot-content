# JeecgBoot 阿里云 OSS 功能总结文档

## 1. 概述

JeecgBoot 框架集成了阿里云 OSS（Object Storage Service）功能，用于实现文件的云存储。该功能允许用户将文件上传到阿里云 OSS，并在系统中管理这些文件。

## 2. 核心组件

### 2.1 OssBootUtil 工具类

`OssBootUtil` 是核心的 OSS 操作工具类，位于 `org.jeecg.common.util.oss` 包中。它提供了以下主要功能：

1. **文件上传**：
   - 支持 MultipartFile 上传
   - 支持 FileItemStream 上传
   - 支持 InputStream 上传
   - 自动生成唯一文件名
   - 支持自定义存储目录和存储桶

2. **文件删除**：
   - 根据 URL 删除文件
   - 根据文件名删除文件

3. **文件访问**：
   - 获取文件流
   - 生成文件外链
   - 获取原始 URL

4. **配置管理**：
   - 设置和获取 OSS 配置参数（endpoint、accessKeyId、accessKeySecret、bucketName、staticDomain）

### 2.2 OssConfiguration 配置类

`OssConfiguration` 是 OSS 的配置类，位于 `org.jeecg.config.oss` 包中。它通过读取配置文件中的参数来初始化 OSS 配置。

### 2.3 OssFile 实体类

`OssFile` 是 OSS 文件的实体类，包含以下字段：
- `fileName`：文件名称
- `url`：文件访问地址

### 2.4 IOssFileService 服务接口

定义了 OSS 文件服务的接口，包括：
- `upload`：文件上传
- `delete`：文件删除

### 2.5 OssFileServiceImpl 服务实现类

实现了 IOssFileService 接口，处理具体的业务逻辑。

### 2.6 OssFileController 控制器

提供了 RESTful API 接口：
- `POST /sys/oss/file/upload`：文件上传
- `DELETE /sys/oss/file/delete`：文件删除
- `GET /sys/oss/file/list`：文件列表查询
- `GET /sys/oss/file/queryById`：根据ID查询文件

## 3. 配置说明

在 application.yml 配置文件中添加以下配置：

```yaml
jeecg:
  oss:
    accessKey: ??
    secretKey: ??
    endpoint: oss-cn-beijing.aliyuncs.com
    bucketName: jeecgdev
    staticDomain: https://static.jeecg.com  # 可选，自定义域名
```

## 4. 功能特点

1. **安全性**：
   - 支持文件类型过滤，防止恶意文件上传
   - 对文件路径进行安全过滤，防止路径遍历攻击
   - 使用阿里云官方 SDK 进行操作

2. **灵活性**：
   - 支持自定义存储桶
   - 支持自定义文件存储目录
   - 支持静态域名配置

3. **易用性**：
   - 提供完整的上传、删除、查询功能
   - 自动生成文件名避免冲突
   - 统一异常处理

4. **兼容性**：
   - 与系统其他存储方式（本地存储、MinIO）并存
   - 可通过配置切换不同的存储方式

## 5. 使用示例

### 5.1 文件上传

```java
@Autowired
private IOssFileService ossFileService;

public void uploadFile(MultipartFile file) throws Exception {
    ossFileService.upload(file);
}
```

### 5.2 直接使用工具类上传

```java
String url = OssBootUtil.upload(multipartFile, "upload/test");
```

### 5.3 使用CommonUtils统一上传接口

在实际业务开发中，推荐使用`CommonUtils.upload`方法进行文件上传，该方法是一个统一的上传接口，可以根据系统配置自动选择上传方式（本地、OSS、MinIO等）：

```java
// 通过配置文件中指定的上传类型进行上传
String url = CommonUtils.upload(file, bizPath, uploadType);
```

其中：
- `file`: MultipartFile类型的文件对象
- `bizPath`: 业务路径，用于指定文件在OSS中的存储目录
- `uploadType`: 上传类型，根据配置文件中的设置决定上传方式

这种方法的优势在于：
1. 统一了不同存储方式的接口调用
2. 可以通过配置文件动态切换存储方式，无需修改代码
3. 业务代码无需关心具体的存储实现细节

### 5.4 文件删除

```
// 通过文件URL删除
OssBootUtil.deleteUrl(file.getUrl());

// 通过文件名删除
OssBootUtil.delete("upload/test/fileName.png");
```

## 6. 注意事项

1. 需要在阿里云控制台创建 OSS 存储桶并获取访问密钥
2. 确保配置的 endpoint 与存储桶所在区域一致
3. 根据实际需求设置 bucket 的访问权限
4. 生产环境中应使用安全的密钥管理方式，避免将密钥直接写入配置文件
5. 在使用`CommonUtils.upload`方法时，确保配置文件中正确设置了`jeecg.uploadType`参数，可选值包括：
   - `local`: 本地存储
   - `oss`: 阿里云OSS存储
   - `minio`: MinIO存储