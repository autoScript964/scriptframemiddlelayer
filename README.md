# <center><a style="color:#454545">脚本框架中间层API</a></center>
#### <center><a style="color:gray; font-size:10pt"> 当前版本: </a><a style="color:brown; font-size:10pt"> v30.07 </a></center>


## <a style="color:#454545">组件/类-目录：</a>
* <a href="#scproxy">ScProxy</a>
* <a href="#CaptureEngine"> CaptureEngine </a>
* <a href="#MatchEngine">MatchEngine</a>
* <a href="#Assets">Assets</a>
* <a href="#ConfigManager"> ConfigManager </a>
* <a href="#DProfiler"> DProfiler </a>
* <a href="#WatchDog"> WatchDog </a>
* <a href="#LogTag"> 日志过滤对照表 </a>


## <a name="scproxy" style="color:#454545">ScProxy组件表简述 </a>

|组件名称|调用Api方法名|可调用|独立线程|描述|
|:----:|:----:|:----:|:----:|:----|
|<a href="#CaptureEngine"> CaptureEngine </a>|captureEngine()|Yes|Yes|截取屏幕组件|
|<a href="#MatchEngine">MatchEngine</a>|matchEngine()|Yes|No|匹配图片|
|<a href="#Assets">Assets</a>|assets()|Yes|No|Assets资源管理|
|<a href="#ConfigManager"> ConfigManager </a>|config()|Yes|No|中间层设置选项|
|<a href="#DProfiler"> DProfiler </a>|profiler()|Yes|Yes|性能分析，需自行开启|
|<a href="#WatchDog"> WatchDog </a>|-|No|Yes|-|

## <a name="CaptureEngine" style="color:#454545"> CaptureEngine </a>
##### 描述
> 截取屏幕组件，由ScProxy启动。启动后通过socket获取虚拟屏幕的截屏信息，默认获取有间隔策略控制，默认策略为1s最多两帧。控制策略可通过 <a href="#ConfigManager">ConfigManager</a> 调整和设置。


##### 公共方法介绍
|方法名称|参数|线程安全|描述|备注|
|:----:|:----:|:----:|----|----|
|getCurrentScreenMat|-|Yes|获取当前截图的mat，类型为<a>BGR_U8C3</a>|<a style="color:red;">注意：</a> 返回的mat为公共mat, 请不(NO!)要自行调用mat.release。如有特别处理需求，请调用clone方法，然后自行管理mat及其内存|
|getCurrentScreenInfo|-|Yes|获取当前截图的screenInfo（兼容类），screenInfo为框架中的info类|<a style="color:red;">注意：</a>该方法为兼容旧版本，不建议调用。使用请自行管理好ScreenInfo的raw数组引用及其内存占用|


##### 示例
```
	Mat mat = ScProxy.captureEngine().getCurrentScreenMat();
	Mat cropedMat = new Mat(mat, new Rect(100, 100, 100, 100));
	...
```
## <a name="MatchEngine" style="color:#454545"> MatchEngine </a>
##### 描述
> 工具类，匹配图片，默认支持匹配请求缓存：同一时间（同一个screenMat）加上相同的参数会返回匹配之前的结果。


##### 公共方法介绍
|方法名称|参数|线程安全|描述|备注|
|:----:|:----:|:----:|----|----|
|doMatching|详见示例|Yes|返回单个匹配图片的结果|第一个参数必须为<a>BGR_U8C3</a>格式的，第二个mat需要自行处理过的。同时要在MatchOpt中设定相关配置|
|Opt|-|Yes|返回一个新的的MatchOpt实例|静态调用|
|Opt(MatOpt)|MatOpt|Yes|返回一个新的<a style="color:purple">MatchOpt</a>实例，参数为<a>MatOpt</a>|<a style="color:red;">注意：</a><a style="color:purple">MatchOpt</a> 为 <a>MatOpt</a> 的包装类|
|OptCompatFromTemplateInfo（兼容）|-|Yes|返回一个新的<a style="color:purple">MatchOpt</a>实例|<a style="color:red;">注意：</a>该方法为兼容旧版本，不建议自行调用。使用请自行管理好ScreenInfo的raw数组引用及其内存占用|

##### 示例
```
	// 二值化图片匹配示例
	Mat screenMt = ScProxy.captureEngine().getCurrentScreenMat();
	
	...
	// 如果asset中的png为工具***处理过***的二值化图片，则直接读取
	Mat tarMat = ScProxy.assets().mat("folk.png");
	MatchOpt matchOption = MatchEngine.Opt().region(100, 100, 10, 10).sim(0.8f)
	.method(MatOpt.Method.HLS);
	...
	// 否则如果asset中的png为原始的裁切RGB图片，则直接读取
	MatOpt matOpt = MatOpt.newIns().method(MatOpt.Method.BIN).binArgs(125, 255, 0);
	Mat tarMat = ScProxy.assets().mat("folk.png", matOpt);
	MatchOpt matchOption = MatchEngine.Opt(matOpt).region(100, 100, 10,  // 注意这里 matOpt作为参数传入
	10).sim(0.8f);
	...
	
	// 最后传入参数匹配
	ScProxy.matchEngine().doMatching(screenMat, tarMat, matchOption);
	
```

## <a name="Assets" style="color:#454545"> Assets </a>
##### 描述
> Assets中拥有LRU缓存实例，用于缓存assets中读取的相关mat。


##### 公共方法介绍

|方法名称|参数|线程安全|描述|备注|
|:----:|:----:|:----:|----|----|
|entity|name|Yes|Entity是包含有info和mat的一个数据类|-|
|info|name|Yes|TemplateInfo|如果asset目录中没有相关的info类型文件，可以在config中设置不读取info|
|mat|name, matOpt(可选)|Yes|获取asset的mat|可选参数MatOpt，可以设置读取mat后的转换的mat，并进行缓存，这个缓存可以和原图缓存同时存在。|

##### MatOpt简介
> MatOpt主要设置用于mat的加载配置。
> 
> 1: method方法需要MatOpt.Method(这是一个枚举类）
> MatOpt.Method：
> > DEFUALT： 默认BGR
> > BIN：二值化
> > HLS:  -
> > HSV:  -
>
>2: binArgs设置二值化参数

##### 示例
```
	// 二值化assets图片匹配示例
	...
	// 如果asset中的png为工具***处理过***的二值化图片，则直接读取
	Mat tarMat = ScProxy.assets().mat("folk.png");

	...
	// 否则如果asset中的png为原始的裁切RGB图片，则直接读取
	MatOpt matOpt = MatOpt.newIns().method(MatOpt.Method.BIN).binArgs(125, 255, 0);
	Mat tarMat = ScProxy.assets().mat("folk.png", matOpt);
	...
	
```

## <a name="ConfigManager" style="color:#454545"> ConfigManager </a>
##### 描述
> 中间层配置类，当前包含有 `Level`  和 ` Printer`  两个模块
> 
> `Level`  主要用来设置包括截屏，ComaptFinder匹配的执行等级
>
> 
> `Printer`当前两个方法主要是为了打印整齐，辅助功能比如下图用 * 号填充的assets名称日志效果
>



##### 公共方法介绍
|方法名称|参数|线程安全|描述|备注|
|:----|:----:|:----:|----|----|
|setAssetsEnableTemplateInfo|boolean|Yes|设置assets是否读取tempalteInfo, 默认为 <a style="color:purple">ture</a>||
|Printer().setMaxAssetSpace|int|Yes|设置CompatPicFinder中日志打印的asset宽度|
|Printer().setAssetPat|UStr.Pat枚举|Yes|设置CompatPicFinder中日志打印的asset宽度填充类型|
|Level().capturing|int|Yes|设置captureEngine最大截屏速率1-10，默认值为2。10为满速无限制，一般情况下，速率跟cpu占用成正比。|
|Level().matching|int|Yes|设置CompatPicFinder时间间隔等级1-10，默认值为5。10为最大间隔，当前间隔值是x10计算，注意：这个间隔只生效于未匹配成功的回合|

##### 示例
```
	ScProxy.config().Printer().setAssetPat(UStr.Pat.SPACE_30); // 空格填充
	ScProxy.config().Printer().setAssetPat(UStr.Pat.STARS_30); //*填充
	ScProxy.config().Printer().setAssetPat(UStr.Pat.LH_30); // - 填充
	ScProxy.config().Printer().setAssetPat(UStr.Pat.HASHTAG_30); // # 填充
	...
```

## <a name="DProfiler" style="color:#454545"> DProfiler </a>
##### 描述
> 用作cpu和内存性能分析的日志打印，独立线程。需要手动调用启动，通过过滤“CMUP”查看
> <a style="color:red"> 注意：当前TOP命令可能因为权限原因打印有问题，过滤不到，请确认包名长度小于15</a>

##### 公共方法介绍
|方法名称|参数|线程安全|描述|备注|
|:----|:----:|:----:|----|----|
|setPackageName|string|Yes|传入包名，如果调用过ScProxy的init函数则不用管这个||
|startWithUserTag|string|Yes|启动性能分析日志线程，参数为自定义过滤条件||

##### 示例
```
	ScProxy.profiler(). setPackageName(mContext.getPackageName());
	ScProxy.profiler(). startWithUserTag("hahaha");
	...
```
## <a name="WatchDog" style="color:#454545"> WatchDog </a>
##### 描述
> 当前仅作用于截屏线程read超时监听和重启

## <a name="LogTag" style="color:#454545"> 日志过滤对照表 </a>
|过滤条件|描述|
|----|----|
|MR|每一个调用CampatPicFinder匹配结果|
|CMUP|性能分析过滤，开启profiler后可见|
|printAllMarks|Dtimer日志输出|