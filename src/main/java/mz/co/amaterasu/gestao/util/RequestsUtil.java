package mz.co.amaterasu.gestao.util;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RequestsUtil {

    public static Set<Long> idsChecker(Set<Long> permissionDTOSet) {
        Set<Long> ids = new HashSet<>();
        permissionDTOSet.stream().map(p -> {
            if (p != null) {
                ids.add(p);
            }
            return  ids;
        }).collect(Collectors.toSet());
        return  ids;
    }

}
