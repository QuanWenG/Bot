﻿# Bot
该项目依赖于java17 Redis

项目build
mvn clean package

项目运行
java -jar bot.jar

static、working、background保持和build成功的jar同级目录
working下的chromedriver.exe保持和谷歌浏览器同版本
background下的图片随意替换

第一次需要修改下生成的配置文件，填上你的master、httpHost、localPicAddress、localPicIp设置下位置，空文件夹绝对路径，不是空文件夹下内容删了概不负责
