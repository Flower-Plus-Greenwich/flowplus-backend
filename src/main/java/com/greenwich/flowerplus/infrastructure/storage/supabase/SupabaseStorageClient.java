package com.greenwich.flowerplus.infrastructure.storage.supabase;//package com.thang.aurea.infrastructure.storage.supabase;
//
//public class SupabaseStorageClient implements SupabaseClient {
//
//    private final SupabaseStorage supabaseStorage;
//
//    public SupabaseStorageClient(SupabaseConfig config) {
//        this.supabaseStorage = new SupabaseStorage(
//                config.getUrl(),
//                config.getServiceRoleKey()
//        );
//    }
//
//    @Override
//    public String upload(byte[] bytes, String path, String contentType) {
//        supabaseStorage.from("product-images")
//                .upload(path, bytes, contentType);
//
//        return supabaseStorage
//                .from("product-images")
//                .getPublicUrl(path);
//    }
//
//    @Override
//    public void delete(String path) {
//        supabaseStorage.from("product-images").remove(path);
//    }
//
//}
