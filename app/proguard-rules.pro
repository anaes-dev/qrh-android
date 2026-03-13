# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep kotlinx serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable data classes
-keep,includedescriptorclasses class dev.anaes.qrh.model.**$$serializer { *; }
-keepclassmembers class dev.anaes.qrh.model.** {
    *** Companion;
}
-keepclasseswithmembers class dev.anaes.qrh.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Preserve line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
