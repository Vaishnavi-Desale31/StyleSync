
package com.project.stylesync_ai.config;

import com.project.stylesync_ai.entity.Category;
import com.project.stylesync_ai.entity.Product;
import com.project.stylesync_ai.repository.CategoryRepository;
import com.project.stylesync_ai.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(DemoDataSeeder.class);

    private record Item(
            String name,
            String description,
            double price,
            String brand,
            String imageUrl,
            String size,
            String color) {
    }

    private record SeedResult(int added, int updated) {
    }

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DemoDataSeeder(CategoryRepository categoryRepository,
                          ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    private static String image(String photoId) {
        return "https://images.unsplash.com/" + photoId
                + "?auto=format&fit=crop&w=800&q=80";
    }

    @Override
    @Transactional
    public void run(String... args) {

        Map<String, Product> existingProducts = new HashMap<>();

        productRepository.findAll().forEach(product -> {
            if (product.getName() != null) {
                existingProducts.putIfAbsent(
                        product.getName().toLowerCase(Locale.ROOT),
                        product
                );
            }
        });

        int added = 0;
        int updated = 0;

        SeedResult result;

        result = seed("Men", existingProducts, List.of(
                new Item("Classic White Oxford Shirt",
                        "Crisp cotton oxford shirt for work and weekends.",
                        1299, "UrbanThread",
                        image("photo-1598033129183-c4f50c736f10"),
                        "M", "White"),

                new Item("Slim Fit Blue Jeans",
                        "Stretch denim jeans with a slim, comfortable fit.",
                        1799, "UrbanThread",
                        image("photo-1542272604-787c3835535d"),
                        "32", "Blue"),

                new Item("Navy Polo T-Shirt",
                        "Soft pique polo with a ribbed collar.",
                        799, "Everwear",
                        image("photo-1521572163474-6864f9cf17ab"),
                        "L", "Navy"),

                new Item("Charcoal Hooded Sweatshirt",
                        "Warm fleece-lined hoodie with a kangaroo pocket.",
                        1599, "Everwear",
                        image("photo-1556821840-3a63f95609a7"),
                        "L", "Charcoal"),

                new Item("Khaki Chino Trousers",
                        "Smart-casual chinos in easy-care cotton twill.",
                        1499, "UrbanThread",
                        image("photo-1473966968600-fa801b869a1a"),
                        "34", "Khaki"),

                new Item("Black Formal Blazer",
                        "Tailored single-breasted blazer for office and events.",
                        3499, "StyleSync",
                        image("photo-1591047139829-d91aecb6caea"),
                        "40", "Black"),

                new Item("Grey Track Pants",
                        "Lightweight jogger track pants with zip pockets.",
                        999, "Everwear",
                        image("photo-1517836357463-d25dfeac3438"),
                        "M", "Grey"),

                new Item("Olive Cargo Shorts",
                        "Relaxed cotton cargo shorts with six pockets.",
                        899, "UrbanThread",
                        image("photo-1591195853828-11db59a44f6b"),
                        "32", "Olive"),

                new Item("Checked Flannel Shirt",
                        "Brushed cotton flannel shirt in a classic check.",
                        1199, "Everwear",
                        image("photo-1603252109303-2751441dd157"),
                        "M", "Red"),

                new Item("Denim Jacket",
                        "Mid-weight denim jacket with button front.",
                        2299, "StyleSync",
                        image("photo-1516257984-b1b4d707412e"),
                        "L", "Indigo")
        ));

        added += result.added();
        updated += result.updated();

        result = seed("Women", existingProducts, List.of(
                new Item("Floral Midi Dress",
                        "Flowy midi dress with a soft floral print.",
                        1899, "Aurelia Lane",
                        image("photo-1496747611176-843222e1e57c"),
                        "M", "Pink"),

                new Item("High-Waist Skinny Jeans",
                        "Stretch denim with a flattering high waist.",
                        1699, "Aurelia Lane",
                        image("photo-1541099649105-f69ad21f3246"),
                        "28", "Dark Blue"),

                new Item("White Cotton Kurti",
                        "Straight-cut cotton kurti with embroidered neckline.",
                        999, "Aurelia Lane",
                        image("photo-1584273143981-41c073dfe8f8"),
                        "M", "White"),

                new Item("Black Wrap Top",
                        "Lightweight wrap top that works day to night.",
                        899, "StyleSync",
                        image("photo-1551163943-3f6a855d1153"),
                        "S", "Black"),

                new Item("Pleated Maxi Skirt",
                        "Elegant pleated skirt with an elastic waist.",
                        1399, "Aurelia Lane",
                        image("photo-1583496661160-fb5886a0aaaa"),
                        "M", "Teal"),

                new Item("Cream Knit Cardigan",
                        "Soft knit cardigan with button front.",
                        1599, "StyleSync",
                        image("photo-1591369822096-ffd140ec948f"),
                        "M", "Cream"),

                new Item("Striped Boat-Neck Tee",
                        "Breathable cotton tee in a classic stripe.",
                        699, "Everwear",
                        image("photo-1503342217505-b0a15ec3261c"),
                        "S", "Navy/White"),

                new Item("Palazzo Pants",
                        "Wide-leg palazzo pants in a fluid fabric.",
                        1099, "Aurelia Lane",
                        image("photo-1594633312681-425c7b97ccd1"),
                        "L", "Mustard"),

                new Item("Formal Shirt Dress",
                        "Belted shirt dress suitable for the office.",
                        2199, "StyleSync",
                        image("photo-1539109136881-3be0616acf4b"),
                        "M", "Sky Blue"),

                new Item("Printed Cotton Dupatta Set",
                        "Kurta, pant and dupatta set in printed cotton.",
                        2499, "Aurelia Lane",
                        image("photo-1515372039744-b8f02a3ae446"),
                        "L", "Maroon")
        ));

        added += result.added();
        updated += result.updated();

        result = seed("Kids", existingProducts, List.of(
                new Item("Kids Graphic T-Shirt",
                        "Soft cotton tee with a fun printed graphic.",
                        449, "LittleLoom",
                        image("photo-1503919005314-30d93d07d823"),
                        "6-7Y", "Yellow"),

                new Item("Kids Denim Dungarees",
                        "Comfortable denim dungarees with adjustable straps.",
                        999, "LittleLoom",
                        image("photo-1519238263530-99bdd11df2ea"),
                        "4-5Y", "Blue"),

                new Item("Girls Party Frock",
                        "Twirl-ready frock with a net skirt and bow.",
                        1299, "LittleLoom",
                        image("photo-1518834107812-67b0b7c58434"),
                        "5-6Y", "Pink"),

                new Item("Boys Cotton Shorts",
                        "Easy pull-on shorts for play and school breaks.",
                        399, "LittleLoom",
                        image("photo-1503454537195-1dcabb73ffb9"),
                        "8-9Y", "Green"),

                new Item("Kids Hooded Jacket",
                        "Light zip-up jacket for cool evenings.",
                        1199, "Everwear",
                        image("photo-1519689680058-324335c77eba"),
                        "7-8Y", "Red"),

                new Item("Kids Pyjama Set",
                        "Two-piece cotton night suit with a cartoon print.",
                        699, "LittleLoom",
                        image("photo-1516627145497-ae6968895b74"),
                        "3-4Y", "Sky Blue"),

                new Item("Boys Checked Shirt",
                        "Half-sleeve cotton shirt for casual outings.",
                        599, "LittleLoom",
                        image("photo-1503919005314-30d93d07d823"),
                        "9-10Y", "Blue/White"),

                new Item("Girls Leggings Pack of 2",
                        "Stretchy cotton leggings, two colours.",
                        549, "LittleLoom",
                        image("photo-1519238263530-99bdd11df2ea"),
                        "6-7Y", "Assorted"),

                new Item("Kids Sweatshirt",
                        "Fleece sweatshirt with a soft inner lining.",
                        849, "Everwear",
                        image("photo-1519689680058-324335c77eba"),
                        "10-11Y", "Grey"),

                new Item("Kids Ethnic Kurta Set",
                        "Festive kurta and pyjama set in soft fabric.",
                        1099, "LittleLoom",
                        image("photo-1516627145497-ae6968895b74"),
                        "5-6Y", "Orange")
        ));

        added += result.added();
        updated += result.updated();

        result = seed("Others", existingProducts, List.of(
                new Item("Canvas Tote Bag",
                        "Sturdy everyday tote with an inner pocket.",
                        499, "StyleSync",
                        image("photo-1544816155-12df9643f363"),
                        "Free Size", "Beige"),

                new Item("Leather Belt",
                        "Genuine leather belt with a brushed buckle.",
                        699, "UrbanThread",
                        image("photo-1553062407-98eeb64c6a62"),
                        "34", "Brown"),

                new Item("Wool Blend Scarf",
                        "Warm, soft scarf for cold weather.",
                        599, "Everwear",
                        image("photo-1520903920243-00d872a2d1c9"),
                        "Free Size", "Maroon"),

                new Item("Cotton Baseball Cap",
                        "Adjustable cap with an embroidered logo.",
                        349, "StyleSync",
                        image("photo-1588850561407-ed78c282e89b"),
                        "Free Size", "Black"),

                new Item("Sports Socks Pack of 3",
                        "Cushioned cotton-blend socks.",
                        299, "Everwear",
                        image("photo-1586350977771-b3b0abd50c82"),
                        "Free Size", "White"),

                new Item("Polarised Sunglasses",
                        "Lightweight frame with UV protection lenses.",
                        899, "StyleSync",
                        image("photo-1511499767150-a48a237f0083"),
                        "Free Size", "Black"),

                new Item("Cotton Handkerchief Set of 5",
                        "Soft, absorbent handkerchiefs in assorted checks.",
                        249, "Everwear",
                        image("photo-1600210492486-724fe5c67fb0"),
                        "Free Size", "Assorted"),

                new Item("Backpack 25L",
                        "Water-resistant backpack with padded straps.",
                        1499, "UrbanThread",
                        image("photo-1553062407-98eeb64c6a62"),
                        "25L", "Navy"),

                new Item("Knitted Beanie",
                        "Stretch-knit beanie for winter.",
                        329, "Everwear",
                        image("photo-1576871337622-98d48d1cf531"),
                        "Free Size", "Grey"),

                new Item("Analog Wrist Watch",
                        "Minimal analog watch with a stainless-steel case.",
                        1799, "StyleSync",
                        image("photo-1523275335684-37898b6baf30"),
                        "Free Size", "Silver")
        ));

        added += result.added();
        updated += result.updated();

        log.info(
                "Demo data seeding finished: {} new product(s) added, "
                        + "{} existing product(s) updated with images.",
                added, updated
        );
    }

    private SeedResult seed(
            String categoryName,
            Map<String, Product> existingProducts,
            List<Item> items) {

        Category category = categoryRepository
                .findByNameIgnoreCase(categoryName)
                .orElseGet(() ->
                        categoryRepository.save(new Category(categoryName)));

        int added = 0;
        int updated = 0;

        for (Item item : items) {

            String key = item.name().toLowerCase(Locale.ROOT);

            Product existingProduct = existingProducts.get(key);

            if (existingProduct != null) {

                if (existingProduct.getImageUrl() == null
                        || existingProduct.getImageUrl().isBlank()) {

                    existingProduct.setImageUrl(item.imageUrl());
                    updated++;
                }

                continue;
            }

            Product product = new Product(
                    item.name(),
                    item.description(),
                    BigDecimal.valueOf(item.price())
                            .setScale(2, RoundingMode.HALF_UP),
                    item.brand(),
                    item.imageUrl(),
                    item.size(),
                    item.color(),
                    category
            );

            Product savedProduct = productRepository.save(product);

            existingProducts.put(key, savedProduct);
            added++;
        }

        return new SeedResult(added, updated);
    }
}
