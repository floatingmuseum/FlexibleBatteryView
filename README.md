# FlexibleBatteryView
provide multiple attributes to customize battery view
***
```xml
<com.floatingmuseum.flexiblebatteryview.FlexibleBatteryView
            android:id="@+id/fbv_battery"
            android:layout_width="50dp"
            android:layout_height="100dp"
            android:layout_marginTop="10dp"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:border_width="2dp"
            app:border_color="@android:color/holo_red_light"
            app:border_corner_radius="4dp"
            app:inside_padding="4dp"
            app:inside_background_color="@android:color/holo_blue_light"
            app:inside_core_radius="2dp"
            app:inside_power_color="@android:color/holo_red_light"
            app:head_height="3dp"
            app:head_width="20dp"
            app:power_level="50"
            app:core_image_scale_type="fitXY"
            app:core_image="@drawable/flash_50_yellow"
            app:direction="up"
            app:text="40"
            app:text_size="30sp"
            app:text_color="@android:color/holo_purple" />
```
***
### Api
|  method   | effect  |
|  ----  | ----  |
| setPowerLevel(powerLevel: Int)  | set battery level,must between 0 to 100 |
| getPowerLevel():Int  | current battery level between 0 to 100 |
| setBorderColor(color: Int)  | customize border color |
| setInsideBackgroundColor(color: Int)  | customize inside background color |
| setInsideCoreColor(color: Int)  | customize battery core color |
| setCoreImage(d: Drawable?)  | image only show when core text is empty and image is not null |
| setText(newText: String)  | core text |
| setTextColor(color: Int)  | core text color |
| setBatteryConfig(config: FlexibleBatteryConfig)  | use this method to modify multiple attributes at the same time，only non-null parameters will be used |
```java
data class FlexibleBatteryConfig(
    val level: Int? = null,
    val borderColor: Int? = null,
    val borderWidth: Float? = null,
    val borderCornerRadius: Float? = null,
    val insideBackgroundColor: Int? = null,
    val insidePaddingWidth: Float? = null,
    val insideCoreColor: Int? = null,
    val insideCoreCornerRadius: Float? = null,
    val headHeight: Float? = null,
    val headWidth: Float? = null,
    val direction: Int? = null,
    val coreImageScaleType: Int? = null,
    val text: String? = null,
    val textColor: Int? = null,
    val textSize: Float? = null,
)
```
***
### Implementation
project build.gradle
```java
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
        google()
    }
}
```
module build.gradle
```java
implementation 'com.github.floatingmuseum:FlexibleBatteryView:v1.0.2'
```
***