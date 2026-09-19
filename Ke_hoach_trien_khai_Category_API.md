# Kế hoạch triển khai Category API (Spring Boot)

2026-09-20 · @Someone

Dự án chia thành 3 phase lớn theo đúng 3 tài liệu đã cung cấp, triển khai theo thứ tự phụ thuộc: xây API trước, thêm Swagger để test, cuối cùng nối AJAX vào giao diện. Nguyên tắc xuyên suốt: mỗi phase chỉ **thêm mới** class/đoạn code cần thiết, không chỉnh sửa code đã có ở phase trước.

## Phase 1 — Xây dựng CRUD API Category (nền tảng)

Là phần lõi, phải làm trước vì Phase 2 và Phase 3 đều phụ thuộc vào các entity/service/controller ở đây. Nguồn: file *HƯỚNG DẪN CRUD API CATEGORY TRÊN SPRING BOOT 3.1.5*.

### 1.1 — Thêm thư viện (pom.xml)

```xml
<!--cấu hình file upload-->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-configuration-processor</artifactId>
  <optional>true</optional>
</dependency>
<dependency>
  <groupId>commons-io</groupId>
  <artifactId>commons-io</artifactId>
  <version>2.11.0</version>
</dependency>
<!-- swagger3 (dùng ở Phase 2.2) -->
<dependency>
  <groupId>io.springfox</groupId>
  <artifactId>springfox-swagger-ui</artifactId>
  <version>3.0.0</version>
</dependency>
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.0.2</version>
</dependency>
```

### 1.2 — Entity

```java
package vn.iotstar.entity;

import java.io.Serializable;
import java.util.Set;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Categories")
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    private String categoryName;
    private String icon;

    @JsonIgnore
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private Set<Product> products;
}
```

```java
package vn.iotstar.entity;

import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Products")
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(length = 500, columnDefinition = "nvarchar(500) not null")
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double unitPrice;

    @Column(length = 200)
    private String images;

    @Column(columnDefinition = "nvarchar(500) not null")
    private String description;

    @Column(nullable = false)
    private double discount;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "YYYY-MM-DD hh:mi:ss")
    private Date createDate;

    @Column(nullable = false)
    private short status;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "categoryId")
    private Category category;
}
```

### 1.3 — Repository

```java
package vn.iotstar.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByCategoryNameContaining(String name);
    Page<Category> findByCategoryNameContaining(String name, Pageable pageable);
    Optional<Category> findByCategoryName(String name);
}
```

```java
package vn.iotstar.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByProductNameContaining(String name);
    Page<Product> findByProductNameContaining(String name, Pageable pageable);
    Optional<Product> findByProductName(String name);
    Optional<Product> findByCreateDate(Date createAt);
}
```

### 1.4 — Model phản hồi (Response)

```java
package vn.iotstar.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private Boolean status;
    private String message;
    private Object body;
}
```

### 1.5 — Tầng Service (Category)

```java
package vn.iotstar.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import vn.iotstar.entity.Category;

public interface ICategoryService {
    void delete(Category entity);
    void deleteById(Long id);
    long count();
    <S extends Category> Optional<S> findOne(Example<S> example);
    Optional<Category> findById(Long id);
    List<Category> findAllById(Iterable<Long> ids);
    List<Category> findAll(Sort sort);
    Page<Category> findAll(Pageable pageable);
    List<Category> findAll();
    Optional<Category> findByCategoryName(String name);
    <S extends Category> S save(S entity);
    Page<Category> findByCategoryNameContaining(String name, Pageable pageable);
    List<Category> findByCategoryNameContaining(String name);
}
```

```java
package vn.iotstar.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.iotstar.entity.Category;
import vn.iotstar.repository.CategoryRepository;

@Service
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public <S extends Category> S save(S entity) {
        if (entity.getCategoryId() == null) {
            return categoryRepository.save(entity);
        } else {
            Optional<Category> opt = findById(entity.getCategoryId());
            if (opt.isPresent()) {
                if (StringUtils.isEmpty(entity.getIcon())) {
                    entity.setIcon(opt.get().getIcon());
                } else {
                    entity.setIcon(entity.getIcon());
                }
            }
            return categoryRepository.save(entity);
        }
    }

    @Override
    public Optional<Category> findByCategoryName(String name) {
        return categoryRepository.findByCategoryName(name);
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public List<Category> findAll(Sort sort) {
        return categoryRepository.findAll(sort);
    }

    @Override
    public List<Category> findAllById(Iterable<Long> ids) {
        return categoryRepository.findAllById(ids);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public <S extends Category> Optional<S> findOne(Example<S> example) {
        return categoryRepository.findOne(example);
    }

    @Override
    public long count() {
        return categoryRepository.count();
    }

    @Override
    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public void delete(Category entity) {
        categoryRepository.delete(entity);
    }

    @Override
    public List<Category> findByCategoryNameContaining(String name) {
        return categoryRepository.findByCategoryNameContaining(name);
    }

    @Override
    public Page<Category> findByCategoryNameContaining(String name, Pageable pageable) {
        return categoryRepository.findByCategoryNameContaining(name, pageable);
    }
}
```

### 1.6 — Chức năng upload file

```java
package vn.iotstar.service;

import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface IStorageService {
    void init();
    void delete(String storeFilename) throws Exception;
    Path load(String filename);
    Resource loadAsResource(String filename);
    void store(MultipartFile file, String storeFilename);
    String getSorageFilename(MultipartFile file, String id);
}
```

```java
package vn.iotstar.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.Exception.StorageException;
import vn.iotstar.config.StorageProperties;

@Service
public class FileSystemStorageServiceImpl implements IStorageService {

    private final Path rootLocation;

    public FileSystemStorageServiceImpl(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public String getSorageFilename(MultipartFile file, String id) {
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        return "p" + id + "." + ext;
    }

    @Override
    public void store(MultipartFile file, String storeFilename) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file");
            }
            Path destinationFile = this.rootLocation.resolve(Paths.get(storeFilename))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new StorageException("Cannot store file outside curent directory");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            throw new StorageException("Failed to store file: ", e);
        }
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            throw new StorageException("Can not read file: " + filename);
        } catch (Exception e) {
            throw new StorageException("Could not read file: " + filename);
        }
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public void delete(String storeFilename) throws Exception {
        Path destinationFile = rootLocation.resolve(Paths.get(storeFilename)).normalize().toAbsolutePath();
        Files.delete(destinationFile);
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            System.out.println(rootLocation.toString());
        } catch (Exception e) {
            throw new StorageException("Could not read file: ", e);
        }
    }
}
```

```java
package vn.iotstar.Exception;

public class StorageException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Exception e) {
        super(message, e);
    }
}
```

```java
package vn.iotstar.Exception;

public class StorageFileNotFoundException extends StorageException {
    private static final long serialVersionUID = 1L;

    public StorageFileNotFoundException(String message) {
        super(message);
    }
}
```

```java
package vn.iotstar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

@Data
@ConfigurationProperties("storage")
public class StorageProperties {
    private String location;
}
```

Thêm vào lớp `...Application.java` (chỉ thêm, không sửa các annotation/method đã có):

```java
@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class) // thêm cấu hình storage
public class SpringbootThymeleafApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootThymeleafApplication.class, args);
    }

    @Bean
    CommandLineRunner init(IStorageService storageService) {
        return (args -> {
            storageService.init();
        });
    }
}
```

### 1.7 — Controller `CategoryAPIController`

```java
package vn.iotstar.Controller.api;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.entity.Category;
import vn.iotstar.model.Response;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.service.IStorageService;

@RestController
@RequestMapping(path = "/api/category")
public class CategoryAPIController {

    @Autowired
    private ICategoryService categoryService;
    @Autowired
    IStorageService storageService;

    @GetMapping
    public ResponseEntity<?> getAllCategory() {
        return new ResponseEntity<Response>(new Response(true, "Thành công", categoryService.findAll()), HttpStatus.OK);
    }

    @PostMapping(path = "/getCategory")
    public ResponseEntity<?> getCategory(@Validated @RequestParam("id") Long id) {
        Optional<Category> category = categoryService.findById(id);
        if (category.isPresent()) {
            return new ResponseEntity<Response>(new Response(true, "Thành công", category.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<Response>(new Response(false, "Thất bại", null), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path = "/addCategory")
    public ResponseEntity<?> addCategory(@Validated @RequestParam("categoryName") String categoryName,
            @Validated @RequestParam("icon") MultipartFile icon) {
        Optional<Category> optCategory = categoryService.findByCategoryName(categoryName);
        if (optCategory.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Category đã tồn tại trong hệ thống");
        } else {
            Category category = new Category();
            if (!icon.isEmpty()) {
                UUID uuid = UUID.randomUUID();
                String uuString = uuid.toString();
                category.setIcon(storageService.getSorageFilename(icon, uuString));
                storageService.store(icon, category.getIcon());
            }
            category.setCategoryName(categoryName);
            categoryService.save(category);
            return new ResponseEntity<Response>(new Response(true, "Thêm Thành công", category), HttpStatus.OK);
        }
    }

    @PutMapping(path = "/updateCategory")
    public ResponseEntity<?> updateCategory(@Validated @RequestParam("categoryId") Long categoryId,
            @Validated @RequestParam("categoryName") String categoryName,
            @Validated @RequestParam("icon") MultipartFile icon) {
        Optional<Category> optCategory = categoryService.findById(categoryId);
        if (optCategory.isEmpty()) {
            return new ResponseEntity<Response>(new Response(false, "Không tìm thấy Category", null), HttpStatus.BAD_REQUEST);
        } else if (optCategory.isPresent()) {
            if (!icon.isEmpty()) {
                UUID uuid = UUID.randomUUID();
                String uuString = uuid.toString();
                optCategory.get().setIcon(storageService.getSorageFilename(icon, uuString));
                storageService.store(icon, optCategory.get().getIcon());
            }
            optCategory.get().setCategoryName(categoryName);
            categoryService.save(optCategory.get());
            return new ResponseEntity<Response>(new Response(true, "Cập nhật Thành công", optCategory.get()), HttpStatus.OK);
        }
        return null;
    }

    @DeleteMapping(path = "/deleteCategory")
    public ResponseEntity<?> deleteCategory(@Validated @RequestParam("categoryId") Long categoryId) {
        Optional<Category> optCategory = categoryService.findById(categoryId);
        if (optCategory.isEmpty()) {
            return new ResponseEntity<Response>(new Response(false, "Không tìm thấy Category", null), HttpStatus.BAD_REQUEST);
        } else if (optCategory.isPresent()) {
            categoryService.delete(optCategory.get());
            return new ResponseEntity<Response>(new Response(true, "Xóa Thành công", optCategory.get()), HttpStatus.OK);
        }
        return null;
    }
}
```

### 1.8 — Kiểm thử API (Postman)

| Method | Endpoint | Kết quả mong đợi |
| --- | --- | --- |
| GET | `/api/category` | `status:true`, `body`: mảng category |
| POST | `/api/category/addCategory` (form-data: categoryName, icon) | `status:true`, category vừa tạo |
| PUT | `/api/category/updateCategory` (form-data: categoryId, categoryName, icon) | `status:true`, category đã cập nhật |
| DELETE | `/api/category/deleteCategory?categoryId=` | `status:true`, category vừa xóa |

### 1.9 — Biến thể cho Spring Boot 2.7.17

Toàn bộ Entity/Repository/Service/Model/Controller ở trên giữ nguyên. Chỉ khác ở cấu hình Swagger (dùng Swagger2 thay vì Swagger3) và cần thêm cấu hình SiteMesh để loại trừ đường dẫn `/api/**` — xem chi tiết ở **Phase 2.1**.

## Phase 2 — Cấu hình Swagger2 & Swagger3

Làm sau Phase 1 vì cần các controller đã có sẵn để Swagger dò và hiển thị. Nguồn: file *CẤU HÌNH SWAGGER2 VÀ SWAGGER 3 TRÊN SPRING BOOT*. Chọn 2.1 hoặc 2.2 tuỳ phiên bản Spring Boot đang chạy — không cần làm cả hai.

### 2.1 — Swagger 2 (Spring Boot 2.7.17)

**Bước 1 — Thêm thư viện (pom.xml)**

```xml
<!-- swagger2 -->
<dependency>
  <groupId>io.springfox</groupId>
  <artifactId>springfox-swagger2</artifactId>
  <version>2.9.2</version>
</dependency>
<dependency>
  <groupId>io.springfox</groupId>
  <artifactId>springfox-swagger-ui</artifactId>
  <version>2.9.2</version>
</dependency>
```

**Bước 2 — Cấu hình Bean Docket**

```java
@Bean
public Docket SWAGGERApi() {
    return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.any())
            .build();
}
```

**Bước 3 — Tiêm Bean vào IOC Container**: thêm `@EnableSwagger2` lên trên class Application (chỉ thêm annotation, không sửa nội dung `main`):

```java
@SpringBootApplication
@EnableSwagger2
public class CosmeticApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CosmeticApiApplication.class, args);
    }

    @Bean
    public Docket SWAGGERApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }
}
```

**Bước 4 — Chạy & kiểm tra**: mở `http://localhost:8188/swagger-ui.html`, danh sách controller (category-controller, cart-controller…) phải hiện ra.

**Ghi chú bắt buộc đi kèm Swagger2** (áp dụng khi dùng chung với giao diện JSP + SiteMesh như ở biến thể Phase 1.9):

Thêm vào `application.properties`:

```
spring.mvc.pathmatch.matching-strategy = ANT_PATH_MATCHER
```

Thêm 2 dòng loại trừ vào `CustomSiteMeshFilter` (chỉ thêm, không sửa các `addDecoratorPath`/`addExcludedPath` đã có):

```java
package vn.iotstar.config;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;

public class CustomSiteMeshFilter extends ConfigurableSiteMeshFilter {
    @Override
    protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
        builder.addDecoratorPath("/*", "/decorators/web.jsp")
                .addDecoratorPath("/admin/*", "/decorators/admin.jsp")
                .addExcludedPath("/login*").addExcludedPath("/login/*")
                .addExcludedPath("/alogin*").addExcludedPath("/alogin/*")
                .addExcludedPath("/api/**").addExcludedPath("/api/**")
                .addExcludedPath("/swagger-ui**").addExcludedPath("/swagger-ui**");
    }
}
```

### 2.2 — Swagger 3 (Spring Boot 3.1.5)

**Bước 1 — Thêm thư viện (pom.xml)** — đã được thêm sẵn ở Phase 1.1, không cần thêm lại:

```xml
<!-- swagger3 -->
<dependency>
  <groupId>io.springfox</groupId>
  <artifactId>springfox-swagger-ui</artifactId>
  <version>3.0.0</version>
</dependency>
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.0.2</version>
</dependency>
```

Springdoc tự động dò controller, không cần Bean `Docket` hay annotation `@Enable...` nào thêm.

**Bước 2 — Chạy & kiểm tra**: mở `http://localhost:8082/swagger-ui/index.html`, phải thấy các nhóm `category-api-controller`, `product-api-controller` với đầy đủ endpoint đã viết ở Phase 1.7.

## Phase 3 — Tích hợp AJAX với RESTful API

Làm sau cùng vì tiêu thụ trực tiếp các endpoint đã xây ở Phase 1 (và có thể xem trước qua Swagger ở Phase 2). Nguồn: file *HƯỚNG DẪN AJAX VỚI RESTFUL API TRONG SPRING BOOT*.

### 3.1 — Thêm thư viện AJAX vào layout

Thêm 2 dòng sau vào thẻ `<head>` của trang layout (`admin.jsp`/`web.jsp`) — chỉ thêm, không xoá các thẻ `<link>`/`<script>` khác đã có:

```html
<script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
<script>var contextPath = "${pageContext.request.contextPath}"</script>
```

Ví dụ đầy đủ `admin.jsp` (Bootstrap 5):

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet"
  integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta2/css/all.min.css"
  integrity="sha512-YWzhKL2whUzgiheMoBFwW8CKV4qpHQAEuvilg9FAn5VJUDwKZZxkJNuGM4XkWuk94WCrrwslk8yWNGmY1EduTA=="
  crossorigin="anonymous" referrerpolicy="no-referrer" />
<link rel="stylesheet" href="/css/mystyles.css" />
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.1.1/css/all.min.css"
  integrity="sha512-KfkfwYDsLkIlwQp6LFnl8zNdLGxu9YAA1QvwINks4PhcElQSvqcyVLLD9aMhXd13uQjoXtEKNosOWaZqXgel0g=="
  crossorigin="anonymous" referrerpolicy="no-referrer" />
<link href="https://unpkg.com/boxicons@2.0.9/css/boxicons.min.css" rel="stylesheet" />
<script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
<script>var contextPath = "${pageContext.request.contextPath}"</script>
<title>Index</title>
</head>
<body>
  <header class="row">
    <div class="col"><%@include file="/common/admin/header.jsp" %></div>
  </header>
  <main class="container-fluid">
    <div class="row mt-4">
      <div class="col mt-5 mb-5"><sitemesh:write property='body'></sitemesh:write></div>
    </div>
  </main>
  <footer class="row">
    <div class="col"><%@include file="/common/admin/footer.jsp" %></div>
  </footer>
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"
    integrity="sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM" crossorigin="anonymous"></script>
</body>
</html>
```

### 3.2 — Bổ sung Controller cho Product (thêm mới, không đụng `CategoryAPIController`)

`CategoryAPIController` đã hoàn chỉnh ở **Phase 1.7**, giữ nguyên. Thêm mới `ProductApiController`:

```java
package vn.iotstar.controllers.api;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.model.ProductModel;
import vn.iotstar.model.Response;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.service.IProductService;
import vn.iotstar.service.IStorageService;

@RestController
@RequestMapping(path = "/api/product")
public class ProductApiController {

    @Autowired
    IProductService productService;
    @Autowired
    ICategoryService categoryService;
    @Autowired
    IStorageService storageService;

    @GetMapping
    public ResponseEntity<?> getAllProduct() {
        return new ResponseEntity<Response>(new Response(true, "Thành công", productService.findAll()), HttpStatus.OK);
    }

    @PostMapping(path = "/addProduct")
    public ResponseEntity<?> saveOrUpdate(
            @Validated @RequestParam("productName") String productName,
            @RequestParam("imageFile") MultipartFile productImages,
            @Validated @RequestParam("unitPrice") Double productPrice,
            @Validated @RequestParam("discount") Double promotionalPrice,
            @Validated @RequestParam("description") String productDescription,
            @Validated @RequestParam("categoryId") Long categoryId,
            @Validated @RequestParam("quantity") Integer quantity,
            @Validated @RequestParam("status") Short status) {
        Optional<Product> optProduct = productService.findByProductName(productName);
        if (optProduct.isPresent()) {
            return new ResponseEntity<Response>(
                    new Response(false, "Sản phẩm này đã tồn tại trong hệ thống", optProduct.get()),
                    HttpStatus.BAD_REQUEST);
        } else {
            Product product = new Product();
            Timestamp timestamp = new Timestamp(new Date(System.currentTimeMillis()).getTime());
            try {
                ProductModel proModel = new ProductModel();
                BeanUtils.copyProperties(proModel, product);
                Category cateEntity = new Category();
                cateEntity.setCategoryId(proModel.getCategoryId());
                product.setCategory(cateEntity);
                if (!proModel.getImageFile().isEmpty()) {
                    UUID uuid = UUID.randomUUID();
                    String uuString = uuid.toString();
                    product.setImages(storageService.getSorageFilename(proModel.getImageFile(), uuString));
                    storageService.store(proModel.getImageFile(), product.getImages());
                }
                product.setCreateDate(timestamp);
                productService.save(product);
                optProduct = productService.findByCreateDate(timestamp);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ResponseEntity<Response>(new Response(true, "Thành công", optProduct.get()), HttpStatus.OK);
        }
    }
}
```

> Lưu ý: `ProductModel`, `IProductService` phải tồn tại (mô hình tương tự `ICategoryService`/`CategoryServiceImpl` ở Phase 1.5) — nếu chưa có, tạo mới theo cùng khuôn mẫu, không sửa `ICategoryService`.

### 3.3 — Hiển thị danh sách Category bằng AJAX (GET)

```html
<table class="table table-striped table-responsive">
  <thead class="thead-inverse">
    <tr><th>Id</th><th>icon</th><th>Name</th><th>Actions</th></tr>
  </thead>
</table>

<script type="text/javascript">
$(document).ready(function() {
  $.getJSON(contextPath + '/api/category', function(json) {
    var tr = [];
    for (var i = 0; i < json.length; i++) {
      tr.push('<tr>');
      tr.push('<td>' + json[i].categoryId + '</td>');
      tr.push('<td>' + '<img src="/admin/categories/images/' + json[i].icon + '" style="width:70px" class="img-fluid" alt=""></td>');
      tr.push('<td>' + json[i].categoryName + '</td>');
      tr.push('<td>' + '<a href="#" data-id="' + json[i].categoryId + '" id="editcate" class="btn btn-outline-warning"><i class="fa fa-edit"></i></a>'
        + '<a href="#" data-id="' + json[i].categoryId + '" id="categoryId" class="btn btn-outline-danger"><i class="fa fa-trash"></i></a>');
      tr.push('</tr>');
    }
    $('table').append($(tr.join('')));
  });
});
</script>
```

> Lưu ý: đoạn `json[i].icon` tham chiếu response GET của **Phase 1.7** — endpoint trả về `Response{status, message, body}`, cần đọc `json.body` thay vì `json` trực tiếp nẽu áp dụng đúng cấu trúc `Response` (tài liệu gốc dùng dữ liệu trả thẳng mảng — kiểm tra lại theo response thực tế trước khi lắp ráp).

### 3.4 — Thêm Category qua Modal + AJAX (POST)

```html
<p><button class="btn btn-success ml-auto" onclick="showCreateNewCategoryModal()"><i class="fas fa-plus mr-2"></i>Thêm Category Ajax</button></p>
<div class="modal" tabindex="-1" role="dialog" id="createCategoryModal">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <form id="addCategory" method="post" onsubmit="return false;" enctype="multipart/form-data">
        <div class="modal-header">
          <h5 class="modal-title">Add Category</h5>
          <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span></button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label for="new_categoryname">Category Name</label>
            <input type="text" class="form-control" id="new_categoryname" name="categoryName">
          </div>
          <div class="form-group">
            <label for="new_icon">Icon</label>
            <input type="file" class="form-control" id="new_icon" name="icon">
          </div>
          <div class="form-group row">
            <div class="col text-center"><button type="submit" class="btn btn-primary btn-block">Add</button></div>
          </div>
        </div>
        <div class="modal-footer"><button type="button" class="btn btn-secondary" data-dismiss="modal">Đóng</button></div>
      </form>
    </div>
  </div>
</div>

<script type="text/javascript">
$("form#addCategory").submit(function(e) {
  e.preventDefault();
  var formData = new FormData(this);
  $.ajax({
    url: contextPath + '/api/category/addCategory',
    type: 'POST',
    dataType: "json",
    data: formData,
    success: function(data) { location.reload(); },
    cache: false,
    contentType: false,
    processData: false
  });
});
function showCreateNewCategoryModal() {
  $('#new_categoryname')[0].value = '';
  $('#createCategoryModal').modal('show');
}
</script>
```

### 3.5 — Cập nhật Category qua Modal + AJAX (PUT)

```html
<div class="modal" tabindex="-1" role="dialog" id="updateCategoryInfoModal">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">Update Category</h5>
        <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span></button>
      </div>
      <div class="modal-body">
        <div class="card">
          <div class="card-header"><h5><i class="far fa-address-card mr-1"></i>Category</h5></div>
          <div class="card-body pb-0">
            <p id="updateCategoryInfoModalId"></p>
            <p id="updateCategoryInfoModalName"></p>
            <p id="updateCategoryInfoModalIcon"></p>
          </div>
        </div>
        <div class="card mt-2">
          <div class="card-header"><h5><i class="far fa-address-card mr-1"></i>Update Category</h5></div>
          <div class="card-body pb-0">
            <form id="updateCategory" method="post" onsubmit="return false;" enctype="multipart/form-data">
              <div class="form-group">
                <label for="categoryName">Category Name</label>
                <input type="text" class="form-control" id="categoryName_up" name="categoryName">
              </div>
              <div class="form-group">
                <label for="new_icon">Icon</label>
                <input type="file" class="form-control" id="icon_up" name="icon">
              </div>
              <input type="hidden" id="categoryId_up" name="categoryId">
              <div class="form-group row">
                <div class="col text-center"><button type="submit" class="btn btn-primary btn-block">Cập nhật</button></div>
              </div>
            </form>
          </div>
        </div>
      </div>
      <div class="modal-footer"><button type="button" class="btn btn-secondary" data-dismiss="modal">Đóng</button></div>
    </div>
  </div>
</div>

<script type="text/javascript">
function showEditCategoryModal(categoryId, categoryName, icon, btn) {
  $('#updateCategoryInfoModalId')[0].innerText = "Category ID: " + categoryId;
  $('#updateCategoryInfoModalName')[0].innerText = "Category Name: " + categoryName;
  $('#updateCategoryInfoModalIcon')[0].innerText = 'Icon: ' + icon;
  $('#categoryName_up')[0].value = categoryName;
  $('#categoryId_up')[0].value = categoryId;
  $('#updateCategoryInfoModal').modal('show');
}
$("form#updateCategory").submit(function(e) {
  e.preventDefault();
  var formData = new FormData(this);
  $.ajax({
    url: contextPath + '/api/category/updateCategory',
    type: 'PUT',
    dataType: "json",
    data: formData,
    success: function(data) { location.reload(); },
    cache: false,
    contentType: false,
    processData: false
  });
});
</script>
```

### 3.6 — Xóa Category bằng AJAX (DELETE)

```html
<a href="#" data-id="json[i].categoryId" id="categoryId" class="btn btn-outline-danger"><i class="fa fa-trash"></i></a>

<script type="text/javascript">
$(document).delegate('#categoryId', 'click', function() {
  var id = $(this).data('id');
  if (confirm('Do you really want to delete record?')) {
    var parent = $(this).parent().parent();
    $.ajax({
      type: "DELETE",
      url: contextPath + '/api/category/deleteCategory?categoryId=' + id,
      dataType: "json",
      data: id,
      success: function() {
        parent.fadeOut('slow', function() { $(this).remove(); });
        location.reload(true);
      },
      error: function() {}
    });
  }
});
</script>
```

### 3.7 — Bài tập mở rông (chưa triển khai)

- [ ] Xây dựng CRUD Product hoàn chỉnh bằng AJAX, theo đúng khuôn mẫu Category ở 3.3–3.6 (danh sách, thêm, sửa, xoá), dùng `ProductApiController` đã thêm ở 3.2.
