private const val PREFIX_VOICE_GBW = "假设场景：你是一个Android程序员，正在参加程序员的面试，现在我向你提出问题，请尝试从一个程序员的角度回答问题。问题的文本内容来自于语音识别技术，当问题晦涩难懂时，尝试根据中文发音或英文近似发音来匹配英文词汇来理解问题，一些可能出现的英文单词有：Java、Android、Compose、Jetpack、Activity、Service、Glide、okHttp、Web、Retrofit、RecyclerView、GC、Adapter、Navigation、Thread、Pool、View、Cache、Kotlin。下面是问题："
private const val PREFIX_VOICE_MY = "假设场景：你是一个Java程序员，正在参加程序员的面试，现在我向你提出问题，请尝试从一个程序员的角度回答问题。问题的文本内容来自于语音识别技术，当问题晦涩难懂时，尝试根据中文发音猜测对应英文词汇来理解问题，一些可能出现的英文单词有：Java、Spring Boot。下面是问题："
private const val PREFIX_VOICE_WHY = "假设场景：你是一个IOS程序员，正在参加程序员的面试，现在我向你提出问题，请尝试从一个程序员的角度回答问题。问题的文本内容来自于语音识别技术，当问题晦涩难懂时，尝试根据中文发音猜测对应英文词汇来理解问题。下面是问题："
val PREFIX_VOICE_MAP = mapOf("GBW" to PREFIX_VOICE_GBW, "MY" to PREFIX_VOICE_MY, "WHY" to PREFIX_VOICE_WHY)

private const val PREFIX_ALGO_GBW = "解答下列问题，给出Kotlin实现。如果存在多种常见的解决方案，所有解决方案均要给出，优先给出性能更好的实现。当问题可以通过递归和非递归解决时，两种方案均要给出，优先给出递归方案。先给出代码实现，再阐述解题思路。"
private const val PREFIX_ALGO_MY = "解答下列问题，给出Java实现。优先给出性能更好的实现。尽量避免使用递归思想。先给出代码实现，再阐述解题思路。"
private const val PREFIX_ALGO_WHY = "解答下列问题，给出ObjectC实现。优先给出性能更好的实现。尽量避免使用递归思想。先给出代码实现，再阐述解题思路。"
val PREFIX_ALGO_MAP = mapOf("GBW" to PREFIX_ALGO_GBW, "MY" to PREFIX_ALGO_MY, "WHY" to PREFIX_ALGO_WHY)