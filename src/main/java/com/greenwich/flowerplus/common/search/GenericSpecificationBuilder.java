package com.greenwich.flowerplus.common.search;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static com.greenwich.flowerplus.common.search.SearchOperation.ZERO_OR_MORE_REGEX;


public class GenericSpecificationBuilder<T> {

    private final List<SpecSearchCriteria> params;

    public GenericSpecificationBuilder() {
        this.params = new ArrayList<>();
    }


    public GenericSpecificationBuilder<T> with(String key, String operation, Object value, String prefix, String suffix) {
        SearchOperation searchOperation = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (searchOperation == SearchOperation.EQUALITY) {
            boolean startWithAsterisk = prefix != null && prefix.contains(ZERO_OR_MORE_REGEX);
            boolean endWithAsterisk = suffix != null && suffix.contains(ZERO_OR_MORE_REGEX);
            if (startWithAsterisk && endWithAsterisk) {
                searchOperation = SearchOperation.CONTAINS;
            } else if (startWithAsterisk) {
                searchOperation = SearchOperation.ENDS_WITH;
            } else if (endWithAsterisk) {
                searchOperation = SearchOperation.STARTS_WITH;
            }
        }
        params.add(new SpecSearchCriteria(null, key, searchOperation, value));
        return this;
    }

    public Specification<T> build() {
        if (params.isEmpty()) return null;

        Specification<T> result = new GenericSpecification<>(params.getFirst());

        for (int i = 1; i < params.size(); i++) {
            result = Boolean.TRUE.equals(params.get(i).getOrPredicate())
                    ? Specification.where(result).or(new GenericSpecification<>(params.get(i)))
                    : Specification.where(result).and(new GenericSpecification<>(params.get(i)));
        }
        return result;
    }
}