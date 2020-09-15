package com.artemis.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@UnstableApi
public @interface ClientOnly {
}
