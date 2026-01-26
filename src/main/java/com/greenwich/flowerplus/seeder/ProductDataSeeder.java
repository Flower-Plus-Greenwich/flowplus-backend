package com.greenwich.flowerplus.seeder;

import com.greenwich.flowerplus.common.enums.*;
import com.greenwich.flowerplus.common.utils.SlugUtils;
import com.greenwich.flowerplus.entity.*;
import com.greenwich.flowerplus.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductDataSeeder {

    private final CategoryRepository categoryRepository;
    private final MaterialRepository materialRepository;
    private final ProductRepository productRepository;

    private static final String SYSTEM_USER = "SYSTEM";

    @Transactional
    public void seed() {
        if (productRepository.count() > 0) {
            log.info("Product data already exists. Skipping product seeder.");
            return;
        }

        log.info("Seeding Product Data with created_by = SYSTEM...");

        // 1. Seed Categories
        Map<String, Category> categories = seedCategories();

        // 2. Seed Materials
        Map<String, Material> materials = seedMaterials();

        // 3. Seed Products
        seedProducts(categories, materials);

        log.info("Product Data Seeding Completed!");
    }

    private Map<String, Category> seedCategories() {
        Map<String, Category> categoryMap = new HashMap<>();

        Category occasionCat = createCategory("Chủ đề", null, CategoryType.OCCASION);
        Category flowerTypeCat = createCategory("Loại hoa", null, CategoryType.FLOWER_TYPE);

        categoryMap.put("BIRTHDAY", createCategory("Sinh Nhật", occasionCat, CategoryType.OCCASION));
        categoryMap.put("ROMANCE", createCategory("Tình Yêu", occasionCat, CategoryType.OCCASION));
        categoryMap.put("GRAND_OPENING", createCategory("Khai Trương", occasionCat, CategoryType.OCCASION));

        categoryMap.put("ROSE", createCategory("Hoa Hồng", flowerTypeCat, CategoryType.FLOWER_TYPE));
        categoryMap.put("TULIP", createCategory("Hoa Tulip", flowerTypeCat, CategoryType.FLOWER_TYPE));
        categoryMap.put("BABY", createCategory("Hoa Baby", flowerTypeCat, CategoryType.FLOWER_TYPE));

        return categoryMap;
    }

    private Category createCategory(String name, Category parent, CategoryType type) {
        Category category = Category.builder()
                .name(name)
                .slug(SlugUtils.toSlug(name))
                .description("Các mẫu hoa đẹp nhất chủ đề " + name)
                .parent(parent)
                .type(type)
                .isActive(true)
                .build();

        // Manual Auditing
        category.setCreatedBy(SYSTEM_USER);
        category.setUpdatedBy(SYSTEM_USER);

        return categoryRepository.save(category);
    }

    private Map<String, Material> seedMaterials() {
        Map<String, Material> materialMap = new HashMap<>();

        materialMap.put("RED_ROSE", createMaterial("Hoa Hồng Đỏ Ecuador", "Cành", new BigDecimal("15000"), MaterialType.FLOWER));
        materialMap.put("BABY_FLOWER", createMaterial("Hoa Baby Trắng", "Bó nhỏ", new BigDecimal("25000"), MaterialType.FLOWER));
        materialMap.put("TULIP_PINK", createMaterial("Tulip Hồng Hà Lan", "Cành", new BigDecimal("35000"), MaterialType.FLOWER));

        materialMap.put("KRAFT_PAPER", createMaterial("Giấy gói Kraft Vintage", "Tờ", new BigDecimal("2000"), MaterialType.ACCESSORY));
        materialMap.put("SILK_RIBBON", createMaterial("Ruy băng lụa đỏ cao cấp", "Mét", new BigDecimal("5000"), MaterialType.ACCESSORY));
        materialMap.put("FOAM", createMaterial("Xốp cắm hoa", "Cục", new BigDecimal("10000"), MaterialType.ACCESSORY));

        return materialMap;
    }

    private Material createMaterial(String name, String unit, BigDecimal costPrice, MaterialType type) {
        Material material = Material.builder()
                .name(name)
                .unit(unit)
                .costPrice(costPrice)
                .sellingPrice(costPrice.multiply(new BigDecimal("1.5")))
                .type(type)
                .imageUrl("https://placehold.co/200x200?text=" + SlugUtils.toSlug(name))
                .build();

        // Manual Auditing
        material.setCreatedBy(SYSTEM_USER);
        material.setUpdatedBy(SYSTEM_USER);

        return materialRepository.save(material);
    }

    private void seedProducts(Map<String, Category> cats, Map<String, Material> mats) {
        createProduct(
                "Bó Hoa Yêu Thương Nồng Cháy",
                "99 đóa hồng đỏ thắm tượng trưng cho tình yêu vĩnh cửu.",
                new BigDecimal("1200000"),
                List.of(cats.get("ROMANCE"), cats.get("ROSE")),
                Map.of(
                        mats.get("RED_ROSE"), 99,
                        mats.get("BABY_FLOWER"), 2,
                        mats.get("KRAFT_PAPER"), 5,
                        mats.get("SILK_RIBBON"), 3
                ),
                List.of("https://placehold.co/600x600?text=Rose+99", "https://placehold.co/600x600?text=Rose+Detail")
        );

        createProduct(
                "Nàng Thơ Mộng Mơ",
                "Bó hoa Tulip hồng nhẹ nhàng, tinh tế dành tặng người thương.",
                new BigDecimal("850000"),
                List.of(cats.get("BIRTHDAY"), cats.get("TULIP")),
                Map.of(
                        mats.get("TULIP_PINK"), 20,
                        mats.get("BABY_FLOWER"), 5,
                        mats.get("KRAFT_PAPER"), 3,
                        mats.get("FOAM"), 1
                ),
                List.of("https://placehold.co/600x600?text=Tulip+Pink")
        );
    }

    private void createProduct(String name, String desc,
                               BigDecimal basePrice,
                               List<Category> categories,
                               Map<Material, Integer> materials,
                               List<String> imageUrls) {

        BigDecimal totalCost = materials.entrySet().stream()
                .map(entry -> entry.getKey().getCostPrice().multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Product product = Product.builder()
                .name(name)
                .slug(SlugUtils.toSlug(name))
                .description(desc)
                .careInstruction("Cắt gốc 45 độ, thay nước mỗi ngày.")
                .basePrice(basePrice)
                .costPrice(totalCost)
                .originalPrice(basePrice.multiply(new BigDecimal("1.2")))
                .status(ProductStatus.ACTIVE)
                .preparedQuantity(10)
                .isMakeToOrder(true)
                .thumbnail(imageUrls.get(0))
                .reviewCount(0)
                .averageRating(0.0)
                .shippingInfo(createDummyShippingInfo())
                .build();

        // Manual Auditing cho Product
        product.setCreatedBy(SYSTEM_USER);
        product.setUpdatedBy(SYSTEM_USER);

        // Add Categories
        categories.forEach(product::addCategory);
        // Lưu ý: ProductCategory được tạo bên trong hàm addCategory của entity Product.
        // Ta cần loop lại list để set auditing cho bảng trung gian này.
        if (product.getProductCategories() != null) {
            product.getProductCategories().forEach(pc -> {
                pc.setCreatedBy(SYSTEM_USER);
                pc.setUpdatedBy(SYSTEM_USER);
            });
        }

        // Add Recipes
        materials.forEach((material, qty) -> {
            ProductRecipe recipe = ProductRecipe.builder()
                    .product(product)
                    .material(material)
                    .quantityNeeded(qty)
                    .build();
            // Manual Auditing cho Recipe
            recipe.setCreatedBy(SYSTEM_USER);
            recipe.setUpdatedBy(SYSTEM_USER);

            product.addRecipe(recipe);
        });

        // Add Assets
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductAsset asset = new ProductAsset();
            asset.setProduct(product);
            asset.setUrl(imageUrls.get(i));
            asset.setType(AssetType.IMAGE);
            asset.setIsThumbnail(i == 0);
            asset.setPosition(i);
            asset.setPublicId("seed_" + UUID.randomUUID().toString().substring(0, 8));

            // Manual Auditing cho Asset
            asset.setCreatedBy(SYSTEM_USER);
            asset.setUpdatedBy(SYSTEM_USER);

            product.getAssets().add(asset);
        }

        productRepository.save(product);
    }

    private ShippingInfo createDummyShippingInfo() {
        return ShippingInfo.builder()
                .weightInGram(500)
                .length(60)
                .width(40)
                .height(20)
                .build();
    }
}