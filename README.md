# 本项目试图降低接入门槛

## 愉快食用需要

1. IntelliJ IDEA

2. 简单 Java 基础，Kotlin 入门：字符串处理、类的继承、方法重写

3. Jsoup 入门

## 食用方法

1. fork 本项目，git clone 到本地，用 IDEA 导入。

1. 继承抽象类 `Parser`，重写它的 `generateCourseList` 方法。

2. `Common` 中抽取了一些可能通用的解析函数，当然你也可以补充。

3. 重写好函数后，新建一个测试的kt文件，如示例中的 `ZhengFangTest.kt`，你运行一下就懂了。

4. commit 进行 [Pull Request](http://www.ruanyifeng.com/blog/2017/07/pull_request.html)

## 注意

1. 建议从项目外引用 html 文件，提交时一定不要上传 html 文件，涉及隐私问题。

2. 其实继承该抽象类，数据来源不一定是 html，可以是 Excel、Json 等等（发挥下想象力）。

## Todo

1. 增加模拟登录导入的模板
