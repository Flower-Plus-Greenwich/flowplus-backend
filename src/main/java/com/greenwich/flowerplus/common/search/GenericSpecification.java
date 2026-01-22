package com.greenwich.flowerplus.common.search;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class GenericSpecification<T> implements Specification<T> {

    private final SpecSearchCriteria specSearchCriteria;

    public GenericSpecification(SpecSearchCriteria criteria) {
        this.specSearchCriteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        // 1. Lấy Path (Hỗ trợ nested như category.id)
        Path<String> path = getPath(root, specSearchCriteria.getKey());

        // 2. Lấy Value
        Object value = specSearchCriteria.getValue();

        return switch (specSearchCriteria.getOperation()) {
            // Các phép so sánh bằng/số
            case EQUALITY -> criteriaBuilder.equal(path, value);
            case NEGATION -> criteriaBuilder.notEqual(path, value);
            case GREATER_THAN -> criteriaBuilder.greaterThan(path, value.toString());
            case LESS_THAN -> criteriaBuilder.lessThan(path, value.toString());

            // 🔥 CÁC PHÉP SEARCH STRING (Fix Case-Insensitive & Nested Path)
            // Phải ép path về lower và value về lower
            case LIKE -> criteriaBuilder.like(
                    criteriaBuilder.lower(path),
                    value.toString().toLowerCase()
            );
            case STARTS_WITH -> criteriaBuilder.like(
                    criteriaBuilder.lower(path),
                    value.toString().toLowerCase() + "%"
            );
            case ENDS_WITH -> criteriaBuilder.like(
                    criteriaBuilder.lower(path),
                    "%" + value.toString().toLowerCase()
            );
            case CONTAINS -> criteriaBuilder.like(
                    criteriaBuilder.lower(path),
                    "%" + value.toString().toLowerCase() + "%"
            );

            // Fix nested cho IN clause
            case IN -> path.in(value);

            default -> null;
        };
    }

    // 🔥 HÀM QUAN TRỌNG: Hỗ trợ Nested Path (category.id)
    private Path<String> getPath(Root<T> root, String attributeName) {
        Path<?> path = root; // Khởi tạo path từ root

        // Nếu key là "category.id" -> Split ra và đi sâu vào từng cấp
        if (attributeName.contains(".")) {
            String[] parts = attributeName.split("\\.");
            From<?, ?> from = root;
            // Iterate all parts except the last one to build joins
            for (int i = 0; i < parts.length - 1; i++) {
                // Default to Left Join to allow finding products even if relation is missing (though for filters usually we want Inner, but generic standard is safe Left)
                // However, for filtering "category.id = 1", if no category, result is null != 1, so filtered out.
                // We shouldn't reuse joins blindly without checking existing ones to avoid basic duplicates but for this simple scope:
                from = from.join(parts[i], JoinType.LEFT);
            }
            path = from.get(parts[parts.length - 1]);
        } else {
            path = root.get(attributeName);
        }
        // Ép kiểu về Path<String> để dùng hàm lower() an toàn
        return (Path<String>) path;
    }
}