package com.greenwich.flowerplus.common.utils;

import com.github.f4b6a3.tsid.TsidCreator;

// 1. Đặt class là 'final' để không ai kế thừa (Đúng)
// 2. Class là 'public' để module User, Post gọi được
public final class TsidUtils {

    private TsidUtils() {}

    public static Long nextId() {
        // getTsid256() là hàm nhanh nhất và thread-safe của thư viện
        return TsidCreator.getTsid256().toLong();
    }

    // (Tùy chọn) Hàm trả về String Base62 nếu cần dùng ở DTO
    // public static String nextIdString() { ... }
}