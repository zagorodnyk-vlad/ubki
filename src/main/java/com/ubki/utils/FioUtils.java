package com.ubki.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FioUtils {

    public static boolean fio(String fio) {
        return fio.matches("(^[^\\s.-][а-яёґєіїА-ЯЁҐЄІЇ'’‘\\- ]{1,80}?-?(?:[-]|[ - ])?[а-яёґєіїА-ЯЁҐЄІЇ\\\\'’‘\\\\-\\\\ -]{1,80}[а-яёґєіїА-ЯЁҐЄІЇ\\\\'’\\\\-\\\\ -]{1,80}([а-яёґєіїА-ЯЁҐЄІЇ\\\\'’‘\\\\-\\\\ -]{2,}))");

    }
}
