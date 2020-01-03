# 코틀린 코루틴 튜토리얼 (Kotlin Coroutines by Tutorials)

<https://store.raywenderlich.com/products/kotlin-coroutines-by-tutorials>

다음의 내용은 `Kotlin Coroutines by Tutorials`을 읽고 주관적으로 간략하게 정리한 내용입니다. 제대로된 학습을 원하신다면 원문을 읽을 것을 권장해 드립니다.

---

## <b>섹션 1. 코루틴 소개</b>

* 이번 색션에서는 멀티스레딩의 문제점을 코루틴을 통해 어떻게 우아하게 해결하는지 살펴본다.
* 리소를 효율적으로 사용하기 위해 suspend, async/await 함수를 사용하는 방법을 배우게 된다.

## `1.1. 비동기 프로그래밍이란 무엇인가?`

* UI(사용자 인터페이스)는 거의 모든 응용 프로그램의 기본 부분이다.
* 애플리케이션은 복잡한 작업을 수행하고 작업이 끝나면 대부분 어떤 형태로든 결과를 보여준다.
* 애플리케이션은 시간이 많이 걸리는 작업을 하더라도 멈춰있는것 처럼 보이지 않게 하기 위해 사용자에게 피드백을 제공할 필요가 있다.

### 피드백 제공

* 이미지를 업로드 하는 프로그램이 있다고 가정한다.
* 업로드 버튼을 누르면 프로그래스바가 돌아 업로드 중임을 알려줘 응용 프로그래미이 중지되지 않았음을 표시한다.

```kotlin
fun uploadImage(image: Image) {
    // Spinner 애니메이션 보여줌
    showLoadingSpinner()
    // 업로드 작업 수행
    uploadService.upload(image)
    // 업로드 작업이 끝나면 Spinner 감추기
    hideLoadingSpinner()
}
```

* 업로드중 피드백이 잘 동작할것 같지만 스피너가 애니메이션이 포함되어 있기 때문에 생각처럼 동작하지 않는다.

```kotlin
fun showLoadingSpinner() {
    showSpinnerView()
    while (running) {
        rotateSpinnerImage()
        delay()
    }
}
```

* uploadService.upload()는 showLoadingSpinner()이 끝나고 시작할거라 가정했지만 코드를 보면 불가능하다.
* showLoadingSpinner()가 blocking call로 만들어져 있기 때문이다.

### 블럭킹 콜

* 작업이 완료할때까지 끝나지 않는 함수를 blocking call이라고 부른다.
* 위의 showLoadingSpinner() 함수는 메인 스레드(UI thread)에서 실행되고 running이 false가 되기 전까지는 완료하지 않는다.
* 애니메이션은 UI thread에서 처리하는게 맞지만 업로드 작업은 UI 작업이 아니므로 오랜작업을 수행할 수 있는 새로운 스레드가 필요하고 멀티 스레딩 매커니즘으로 해결할 수 있다.

### 메인 스레드와 워커 스레드

* 메인 스레드 혹은 UI 스레드는 UI를 관리하는 책임을 지니고 모든 응용프로그램은 교착상태를 피하기 위해 하나의 메인 스레드만을 가진다.
* UI 렌더링을 담당하지 않는 스레드를 백그라운드 혹은 워커 스레드한다.
* 복수의 제어 스레드의 실행을 허용하는 능력을 멀티스레딩이라고 하며, 이들의 협업과 동기화를 제어하는 데 사용되는 기법의 집합을 동시성이라고 한다.

### 백그라운드와 UI 스레드 상호작용

* spinner 이미지 회전, 이미지를 업로드 완료 통지를 메인 스레드에 전달해 회전된 이미지를 다시그리거나, 업로드 완료여부를 표시하는 일련의 동시성 작업들이 블럭킹 없이 잘 동작하기 위해서는 두 스레드간에 상호작용이 필요하다.

### 데이터 공유

* 통신을 하기 위해서는 서로 다른 두 스레드간 데이터를 공유해야할 필요가 있다.
* 두 스레드간 안전하게 데이터를 처리하기 위해서는 동기화라는 작업이 필요하다.
* 여러 스레드에서 동일한 데이터에 액세스하여 올바른 동작과 우수한 성능을 유지하는 것이 동시 프로그래밍의 진정한 과제이다.
* 특별한 케이스로 읽기만 하고 업데이트를 하지 않는 경우도 있을 수 있는데 이때는 불변 자료형을 사용하면 항상 race condition 없이 동일한 데이터를 읽을 수 있다.
* 스레드의 데이터를 안전하게 공유하기 위해 사용할 수 있는 데이터 구조는 무엇일까? 가장 중요한 데이터 구조로는 큐와 파이프라인이 있다.

### 큐

* 스레드는 보통 큐를 이용해 통신을 하며 생산자/소비자 패턴으로 작용할 수 있다.
* 생산자 스레드에서는 정보를 넣기만 하고 소비자 스레드에서는 이를 가져와 처리하는 역할을 담당한다.
* 스레드는 일반적으로 공유할 정보를 캡슐화하는 메시지라는 객체로 데이터를 대기열에 넣는다.
* 블러킹 큐는 스레드가 사용 가능한 경우에만 메시지를 소비할 수 있도록 동기화 기능을 제공한다.

### 파이프라인

* 파이프나 수도꼭지처럼 프로그래밍에서 파이프라인이나 파이프에도 동일한 프로세스가 사용된다.
* 데이터 스트림을 흐르게 하는 파이프가 있고 청취자도 있다. 데이터는 대개 바이트의 흐름이다.

### 콜백을 사용하여 작업 완료 처리

* 모든 비동기 프로그래밍 메커니즘 중에서 콜백(callback)이 가장 자주 사용된다.

```kotlin
interface OnUploadCallback {
    fun onUploadCompleted()
}
```

* 오래 걸리는 작업이 완료되었을때 콜백을 실행하게 되고 코틀린과 같은 현대 프로그래밍 언어에서는 람다 표현으로 사용할 수 있다.
* 람다에는 업로드 작업이 완료될 때 실행할 코드가 포함되어 있을 것이다.
* 작업 결과로 어떤 값이 반환되는 경우 람다 블록으로 전해져 안에서부터 사용할 수 있다.

```kotlin
fun uploadImage(image: Image) {
    showLoadingSpinner()
    uploadService.upload(image) { hideLoadingSpinner() }
}
```

### Indentation hell

* 콜백은 스레드간 통신에 사용되는 심플한 방법이지만 여러 함수 중첩 콜백 사용시 많은 {}가 사용되어져 계단구조처럼 깊어져 관리하는데 어려움을 겪을 수 있다.
* 콜백 기반 접근법의 가장 중요한 문제는 각 함수의 결과 데이터를 전달하는 것이다. 데이터를 전달하기 위해서는 중첩된 콜백을 사용해야 하는데, 이는 코드를 읽고 유지하기 어렵게 만든다.

```kotlin
fun uploadImage(imagePath: String) {
    showLoadingSpinner()

    loadImage(imagePath) { image ->
        resizeImage(image) { resizedImage ->
            uploadImage(resizedImage) {
                hideLoadingSpinner()
            }
        }
    }
}
```

### Reactive Extension을 사용해 백그라운드 처리

* 비동기 처리시 중대한 중첩 콜백에 대한 문제를 해결하기 위해 reactive extensions이 등장한다.
* Rx는 비동기 처리를 이벤트 스트림으로 감싸 큐, 파이프라인 처럼 데이터를 처리한다.
* Rx에는 스트림 데이터를 깔끔하게 처리하도록 많은 수의 연산자를 제공하고 함수체인 안에서 오류를 처리할 수 있다.

```kotlin
fun uploadImage(imagePath: String) {
    loadImage(imagePath)
        .doOnSubscribe(::showLoadingSpinner)
        .flatMap(::resizeImage)
        .flatMapCompletable(::uploadImage)
        .subscribe(::hideLoadingSpinner, ::handleError)
}
```

* 위의 코드가 처음에는 이상하게 보일 수 있으나 연산자를 이해하고 있다면 굉장히 깔끔히 처리했다는것을 알 수 있다.
* 특히 에러 발생시 에러 또한 스트림으로 오기 때문에 onError lambda에서 처리한 모습을 볼 수 있다. 만일 콜백이었다면 직접 try/catch를 사용해 처리해야 했을 것이다.

### Rx의 복잡성에 대한 심층 분석

* Rx는 비동기 프로그래밍을 깨끗하고 읽을 수 있게 만들 수 있다.
* Rx는 강력한 비동기 메커니즘으로 스트림의 오류 처리 개념은 애플리케이션에 추가적인 안전성을 더하게 해준다.
* Rx를 익히기 위해서는 학습곡선, 여러가지 추가 개념, 새로운 패러다임에 대한 이해가 필요하므로 완벽한것으로민 볼수는 없다.

### 과거로 부터 폭발

* 코루틴은 비동기 프로그래밍을 다루는 독특한 방법으로 이를 묘사하는 1960년대로 거슬러 올라가는 메커니즘이다.
* 이 개념은 언어의 일급객체 개념으로 suspension points, suspendable functions and continuations을 사용하는 것을 중심으로 한다. (다소 추상적이다)

```kotlin
fun fetchUser(userId: String) {
    val user = userService.getUser(userId) // 1

    print("Fetching user") // 2
    print(user.name) // 3
    print("Fetched user") // 4
}
```

* 위 코드에서 중요한 논리적인 순서는 1은 3보다 무조건 전에 실행되어져야 한다는 것이다.
* 사용자 데이터가 실제로 표시되기 전에 사용자 데이터의 가져오기를 편리한 시간으로 지연시킬 수도 있다.
* 그것들은 부분 스레드, 부분 콜백 메커니즘으로 스케줄링과 중단이라는 시스템 기능을 사용한다.
* 이로써 콜백, 스레드, 스트림을 사용하지 않고 blocking call을 즉시 반환할 수 있다.

### 코루틴 탐험

* 코루틴은 마법이 아니라 로우 레벨 기능을 잘 사용하는 방법일 뿐이다.
* getUser()가 suspendable functions로 마크되어 있다면 시스템은 백그라운드에서 호출을 준비한다.
* 결과를 얻을때까지 차단하거나 중단하고 기다릴 있는데 이를 awaiting이라고 부른다.
* 코루틴에서는 비동기 코드를 작성하는 것이 매우 간단하기 때문에, 복수의 요청이나 데이터 변환을 쉽게 결합할 수 있다.
* 콜백이니, 스트림이니 하는 복잡한 연산자 없이 함수를 suspendable functions로 표시하고 코루틴 블록으로 호출하기만 하면 된다.
* 코루틴은 오래되었지만 강력한 개념으로 여러 프로그래밍 언어에서 그 구현의 버전을 발전시켜 왔다.

### 키포인트

* 멀티스레딩은 동시에 여러개의 일을 실행할 수 있다.
* 비동기 프로그래밍은 스레드간 통신 중 일반적인 패턴이다.
* 스레드간 데이터를 공유하는 매커니즘에는 여러 종류가 있는데 그중에는 큐, 파이프라인이 있다.
* 콜백은 복잡하고 유지보수가 어려운 매커니즘이다.
* Rx는 데이터 전송, 조합, 에러핸들링에 클린한 방법을 제공해주지만 모든 애플리케이션에 적합한것은 아니다.
* 코루틴은 로우레벨 스케줄링에 기초를 둔 안정적인 컨셉이다.
* 코루틴은 항상 새로운 스레드를 만들지 않고 스레드 풀 이미 있는 스레드를 재사용한다.
* 코루틴을 통해 깨끗하고 순차적인 스타일로 비동기 코드를 작성할 수 있다.

### Where to go from here?

* suspendable functions과 suspension points를 배우게 된다.
* 코틀린에서 코루틴을 어떻게 만들었는지 배우게 된다.
* 결과를 반환하는 비동기 호출을 만들고 결과를 기다리는 방법을 배우게 된다.

## `1.2. 빌드환경 세팅`

### JDK 구성

* Intellij는 순수 Kotlin, Java 프로젝트 및 스프링 플러그인도 지원하여 다양한 프로젝트를 사용할 수 있다.
* Intellij는 JVM 환경을 필요로 하므로 먼저 Java Development Kit(JDK) 설정이 필요하다.
* 다운로드 주소 (https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* 책에서 일부 기능은 안드로이드를 기반으로 하므로 1.8까지만 지원하는 안드로이드에 맞춰 1.8을 사용하도록 한다.

### Intellij IDEA 설치

* 이 책에서는 강력한 기능을 지원하는 Intellij를 사용한다.
* 다운로드 주소 (https://www.jetbrains.com/idea/download)
* 설치 후 이전 설정이 있다면 설정을 가져오도록 한다.
* Kotlin JVM 프로젝트 생성하는 방법을 알아보도록 한다.

```txt
1. Intellij IDEA 팝업 창에서 Create New Project 선택한다.
2. 프로젝트 타입을 선택하고 Kotlin > Kotlin/JVM 다음으로 이동한다.
3. 프로젝트 이름, 위치, SDK, Kotlin Runtime을 선택하고 완료한다.
```

### 안드로이드 환경설정 구성

* 추후 코루틴을 안드로이드 멀티스레딩 환경에서 구현할수 있으므로 안드로이드 스튜디오를 설치하도록 한다.
* 다운로드 주소 (https:// developer.android.com/studio)
* 안드로이드 새 프로젝트를 시작하는 방법을 알아보도록 한다.

```txt
1. Start a new Android Studio project를 선택한다.
2. 두번째 스텝으로 이동하여 API Level를 21로 선택한다.
3. 최종 스텝으로 이동하여 Empty Application 옵션을 선택한 후 다음으로 진행하여 완료한다.
```

### 프로젝트 가져오기

* Intellij에서 새로운 프로젝트 시작이 아닌 기존 프로젝트를 가져올 수 도 있다.

```txt
1. Import Project를 선택한다.
2. 폴더를 선택하는 팝업 창이 뜨면 기존 프로젝트 폴더를 선택한 후 Open을 누른다.
3. 프로젝트 타입을 선택하는 창이 뜨면 Gradle를 선택하고 다음으로 이동한 후 완료를 누른다.
```

### 키포인트

* Intellij IDEA는 JVM 개발환경이다.
* 안드로이드 스튜디오는 완전히 통합된 Gradle 빌드시스템을 사용한다.

### Where to go from here?

* 환경설정을 완료함으로 프로젝트를 수행할 수 있게 되었다.
* 몇몇 세팅을 프로젝트를 시작으로 연습코드 작업을 시작할 수 있다.

## `1.3. 코루틴 시작`

* routines과 실행흐름을 어떻게 제어하는지 배운다.
* suspendable funtions과 suspension points에 대해 배운다.
* 몇가지 작업을 생성하여 UI 스레드에 게시하는 법을 배운다.

### routines 실행

* 프로그램에는 main 함수와 같은 주 루틴이 있고 그 main 블럭에서 서브루틴을 호출할 수 있다.
* 기본적으로 서브루틴은 블럭킹 콜로 동작하지만 코루틴에서는 넌블럭킹 콜로 부를 수 있다.
* 넌블럭킹 콜은 코드가 병행으로 실행된다는 것을 의미한다.

### 코루틴 실행

* [소스코드](sources/getting_started_with_coroutines)

> Main.kt
```kotlin
fun main() {
    (1..10000).forEach {
        GlobalScope.launch {
            val threadName = Thread.currentThread().name
            println("$it printed on thread ${threadName}")
        }
    }
    Thread.sleep(1000)
}
```

* 10000개의 코루틴을 실행해도 코루틴은 가볍기 때문에 성능저하 없이 잘 수행된다.
* 스레드였다면 대부분 OutOfMemory를 냈을것이다.
* 코루틴 바디는 코루틴 빌더라고 불리는 launch() 파라이터로 전달되는 코드 블럭이다.
* 코루틴을 실행할때 실행시점의 라이프사이클과 관계없는 백그라운드 CoroutineScope를 가진다. 위에서는 GlobalScope를 가지고 이는 애플리케이션 라이프사이클에 묶이게 된다.

### 코루틴 생성

* 코루틴 실행을 위해서는 코루틴 빌더를 사용해야 한다.
* 코루틴 라이브러리는 여러개의 코루틴 빌더를 가지고 있고 이를 통해 코루틴을 시작할 수 있게 해준다.
* launch() 함수원형은 보면 다음과 같다.

```kotlin
public fun CoroutineScope.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job
```

* launch는 CoroutineContext, CoroutineStart, 람다함수 세개의 파라미터를 가진다.
* CoroutineContext, CorutineStart는 기본값으로 세팅되어 있으므로 세팅 필수값은 아니다.
* CoroutineStart 옵션에는 DEFAULT, LAZY, ATOMIC, UNDISPATCHED가 있다.
* 마지막 람다함수(CoroutineScope 타입 리시버)에는 코루틴이 실행할 코드를 넣어주면 된다.
* 코틀린은 suspendable functions 개념으로 만들어져 있고 마지막 람다함수는 suspend로 정의되어 있다.

### 코루틴 범위지정

* 코루틴은 메인 실행흐름과 병행해서 실행되나 메인 실행흐름이 종료되거나 멈춘다고 해서 코루틴도 동일하게 동작한다는 뜻이 아니다.
* 서로 다른 라이프사이클은 프로그램의 미묘한 버그룰 유발할 수 있다.
* 서로 다른 라이프사이클을 해결하고자 코루틴은 CoroutineScope를 만들었고 이 자체 라이프사이클이 종료되면 진행중이던 모든 작업이 중지된다.
* 코루틴에서 launch() 호출하는 방법에는 두가지가 있고 상황에 맞게 사용하면 된다.

```txt
1. 코루틴이 어디서 실행되는지 아무 상관없는 GlobalScope를 사용 (결과나 UI스레드 처리에 대해 신경쓰지 않을때)
2. CoroutineScope 인터페이스를 구현하여 CoroutineContext 인스턴스를 제공 (UI스레드에 결과를 반영하거나 Activity 라이프사이클에 바인딩 해야될때)
```

### Jobs 설명

* 코드를 생성 후 실행하는 것, launch()의 결과를 가르켜 Job이라고 부른다.
* launch() 함수 실행시 람다로 전달한 코드가 바로 실행되는 것이 아니라 큐에 삽입된다.
* Job이란 큐에 있는 코루틴에 대한 핸들을 의미하고 몇개의 필드, 함수와 확장성을 가지고 있다.
* Job끼리 종속관계를 가질 수 있다. 예를들어, Job A에서 Job B로 join() 한다면 B 완료전까지 전자가 실행되지 않는다는것을 의미한다.
* 특별한 코루틴 빌더를 사용하여 부모-자식 관계를 설정할 수도 있고 자식 Job들이 완료되지 않으면 부모 Job도 완료되지 않는다.
* Job 추상화는 상태 정의를 통해 가능하고, 코루틴이 launch() 하면 새로운 Job이 만들어지고 항상 New 상태가 된다.
* 코루틴 생성시 CorutineStart 파라미터로 LAZY를 사용하지 않았다면 New 상태에서 기본적으로 Active 상태로 바로 전환된다.
* start(), join()을 통해 New에서 Active 상태로 변경이 가능하다.
* 실행중인 코루틴은 항상 Active 상태이고 Job은 complete이나 cancaled가 될 수 있다.
* Job은 자식들이 complete 될때까지 Completing 상태로 남아있을 수 있다.
* State는 코루틴이 어떻게 돌아가는지와 무엇을 할 수 있는지를 알 수 있게 해주는 기본적인 정보이다.

### Jobs 취소

* 코루틴 launch() 후 Job이 생성되면 예외 혹은 어플리케이션 상황으로 인해 Job을 취소해야 되는 여러 상황들이 발생할 수 있다.
* 네트워크를 통해 이미지를 받아 보여주는 작업을 코루틴을 통해 한다고 가정할때 네트워크가 연결이 안되거나 스크롤 리스트에서 사라진경우 필요한 취소작업을 코루틴에서 어떻게 처리해야하는지에 대해 이해하는 것이 매우 중요하다.
* 코루틴은 suspending behavior 기능을 가지고 있어 시스템을 다운 시킬 수 있는 uncaught exception도 일시 중단하여 나중에 관리할 수 있다.

## `1.4. Suspending 함수`

* 

## `1.5. Async/Await`

* 

## `1.6. 코루틴 Context`

* 

## `1.7. 코루틴 Context와 Dispatchers`

* 

## `1.8. 예외처리`

* 

## `1.9. 취소 관리하기`

* 

---