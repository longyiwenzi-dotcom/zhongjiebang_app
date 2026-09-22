package cn.hrbzhongjiebang.common;

import java.util.regex.Pattern;

public record PhoneNumber(String value) {
    private static final Pattern MAINLAND_MOBILE = Pattern.compile("^1[3-9]\\d{9}$");

    public PhoneNumber {
        if (value == null || !MAINLAND_MOBILE.matcher(value).matches()) {
            throw new BusinessException("INVALID_PHONE", "手机号格式不正确");
        }
    }
}
