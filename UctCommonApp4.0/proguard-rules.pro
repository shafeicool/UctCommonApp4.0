# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\AndroidSdk\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;

-optimizationpasses 5          #指定代码的压缩级别 0 - 7
-dontusemixedcaseclassnames   # 是否使用大小写混合
-dontpreverify           # 混淆时是否做预校验
-verbose                # 混淆时是否记录日志
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  # 混淆时所采用的算法
-keepattributes *Annotation* #假如项目中有用到注解，应加入这行配置
-keepattributes Signature # 过滤泛型
-keepattributes EnclosingMethod #用到了反射需要加入
-keepattributes Exceptions

-keep public class * extends android.app.Activity      # 保持哪些类不被混淆
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Application   # 保持哪些类不被混淆
-keep public class * extends android.app.Service       # 保持哪些类不被混淆
-keep public class * extends android.content.BroadcastReceiver  # 保持哪些类不被混淆
-keep public class * extends android.content.ContentProvider    # 保持哪些类不被混淆
-keep public class * extends android.app.backup.BackupAgentHelper # 保持哪些类不被混淆
-keep public class * extends android.preference.Preference        # 保持哪些类不被混淆
-keep public class com.android.vending.licensing.ILicensingService    # 保持哪些类不被混淆

-dontwarn android.support.**
-keep public class * extends android.support.**{*;}

-keep class * implements java.io.Serializable
-keep class com.ptyt.uct.entity.**
-keep class com.android.uct.bean.**
-keep class com.ptyt.uct.common.AppUrl
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keepclassmembers class **.R$* { # 不混淆R类里及其所有内部static类中的所有static变量字段
    public static <fields>;
}
-keep class **.Webview2JsInterface { *; }  # 保护WebView对HTML页面的API不被混淆
-keepclassmembernames class com.ptyt.uct.entity.** { *; }# 转换JSON的JavaBean，类成员名称保护，使其不被混淆
# 针对v4/v7包
-dontwarn android.support.**
-dontwarn android.support.v4.**
-keep class android.support.** { *; }
-keep public class * extends android.support.**

# 下面都是项目中引入的第三方jar包,第三方jar包中的代码不是我们的目标和关心的对象，故而对此我们全部忽略不进行混淆
# libraryjars:指定要处理的应用程序jar,war,ear和目录所需要的程序库文件
# dontwarn:不考虑警告问题
#-libraryjars libs/activation.jar # additionnal 发邮件
-dontwarn com.sun.activation.registries.**
-dontwarn javax.activation.**
-keep class com.sun.activation.registries.**{*;}
-keep class javax.activation.**{*;}

# -libraryjars libs/additionnal.jar # additionnal 发邮件
-dontwarn myjava.awt.datatransfer.**
-dontwarn org.apache.harmony.**
-keep class myjava.awt.datatransfer.**{*;}
-keep class org.apache.harmony.**{*;}

-dontwarn com.amap.** # 高德地图
-dontwarn com.autonavi.**
-keep class com.amap.**{*;}
-keep class com.autonavi.**{*;}

# -libraryjars libs/android_screen.jar # 屏幕适配
-dontwarn com.dqqdo.androidscreen.**
-keep class com.dqqdo.androidscreen.**{*;}

# -libraryjars libs/fastjson-1.2.39.jar # fastjson
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.**{*;}

# -libraryjars libs/mail.jar # mail 发邮件
-dontwarn com.sun.mail.**
-dontwarn javax.mail.**
-keep class com.sun.mail.**{*;}
-keep class javax.mail.**{*;}

# -libraryjars libs/UctJar_4.0.3.jar # 自己的jar
-dontwarn com.android.uct.**
-keep class com.android.uct.**{*;}
-dontwarn com.nclient.nclientcore.**
-keep class com.nclient.nclientcore.**{*;}
-dontwarn com.sun.jna.**
-dontwarn com.facebook.stetho.**
-keep class com.facebook.stetho.**{*;}
-keep class com.sun.jna.**{*;}

-dontwarn de.mindpipe.android.logging.log4j.**
-dontwarn org.apache.**
-keep class de.mindpipe.android.logging.log4j.**
-keep class org.apache.**{*;}

-keep class org.greenrobot.greendao.**{*;} # greendao
-keep class net.sqlcipher.**{*;}
-dontwarn javax.**
-dontwarn org.**
-dontwarn freemarker.**
-dontwarn java.rmi.**
-keep class javax.**{*;}
-keep class org.**{*;}
-keep class java.beans.**{*;}
-keep class freemarker.**{*;}
-keep class java.rmi.**{*;}
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties
-keep class **javax.annotation.**{*;}
-dontwarn retrofit2.** # retrofit2
-keep class retrofit2.** { *; }
-keep class com.nostra13.universalimageloader.** { *; } #imageLoader包下所有类及类里面的内容不要混淆

-dontwarn rx.**
-keep class rx.** {*;} # rxjava
-keep class sun.**{*;}

-dontwarn net.soureceforge.pinyin4j.** # pinyin4j
# -libraryjars libs/pinyin4j-2.5.1.jar
-keep class net.sourceforge.pinyin4j.** { *;}

-keepclassmembers class ** {@org.greenrobot.eventbus.Subscribe <methods>;} # eventbus
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {# Only required if you use AsyncExecutor
    <init>(java.lang.Throwable);
}
-keep class com.bumptech.glide.** {*;} # glide
-keep class android.support.multidex.** {*;} # multidex

-keepclasseswithmembernames class * {  # 保持 native 方法不被混淆
    native <methods>;
}
-keepclasseswithmembers class * {   # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {# 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers public class * extends android.view.View {# 所有View的子类及其子类的get、set方法都不进行混淆
   void set*(***);
   *** get*();
}
-keep class * implements android.os.Parcelable {  # 不混淆Parcelable和它的子类，还有Creator成员变量
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * extends android.app.Activity { # 不混淆Activity中参数类型为View的所有方法
    public void *(android.view.View);
}
-keepclassmembers enum * {     # 不混淆Enum类型的指定方法
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.huawei.** {*;}
-dontwarn com.huawei.**
-keep class com.google.** {*;}
-dontwarn com.google.**
-keep class com.handmark.** {*;}
-dontwarn com.handmark.**
-keep class com.squareup.** {*;}
-dontwarn com.squareup.**
-keep class okio.Deflater.** {*;}
-dontwarn okio.Deflater.**
-keep class okio.DeflaterSink.** {*;}
-dontwarn okio.DeflaterSink.**
-keep class okio.Okio.** {*;}
-dontwarn okio.Okio.**
-keep class org.codehaus.** {*;}
-dontwarn org.codehaus.**
-keep class okio.** {*;}
-dontwarn okio.**
-keep class android.net.http.** {*;}
-dontwarn android.net.http.**
-keep class org.apache.** {*;}
-dontwarn org.apache.**
-keep class com.googlecode.** {*;}
-dontwarn com.googlecode.**
-keep class de.greenrobot.event.** {*;}

-keepclassmembers class ** {
    public void onEvent*(**);
    void onEvent*(**);
}
-keep class com.bumptech.glide.integration.okhttp.OkHttpGlideModule # Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.google.gson.** {*;}
-dontwarn com.google.gson.**
-keep class com.alibaba.fastjson.** { *; }
-dontwarn com.alibaba.fastjson.**
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
