# 코틀린 코루틴 튜토리얼 (Kotlin Coroutines by Tutorials)

<https://store.raywenderlich.com/products/kotlin-coroutines-by-tutorials>

다음의 내용은 `Kotlin Coroutines by Tutorials`을 읽고 주관적으로 간략하게 정리한 내용입니다. 제대로된 학습을 원하신다면 원문을 읽을 것을 권장해 드립니다.

---

## <b>섹션 1. 코루틴 소개</b>

* 이번 색션에서는 멀티스레딩의 문제점을 코루틴을 통해 어떻게 우아하게 해결하는지 살펴본다.
* suspend 함수와 async/await 함수를 효율적으로 사용하는 방법을 배우게 된다.

## `1.1. 비동기 프로그래밍이란?`

* UI를 가지는 프로그램에서 오래걸리는 작업으로 인해 화면이 멈춰있어서는 안된다.
* 복잡하거나 오래걸리는 동기적 작업의 문제점을 비동기 프로그래밍을 통해 해결할 수 있다.

### 피드백 제공

* 이미지를 업로드 하는 프로그램이 있다고 가정한다.
* 업로드시 화면이 멈춰 있으면 사용자 경험이 좋지 않으므로 업로드 버튼을 누르면 프로그래스바가 돌아 업로드 중임을 알려준다.
* 어떻게 하면 사용자에게 피드백을 주면서 업로드가 가능할까?

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

* 잘 동작할것 같지만 스피너가 애니메이션을 가지고 있기 때문에 정상동작하지 않는다.

```kotlin
fun showLoadingSpinner() {
    showSpinnerView()
    while (running) {
        rotateSpinnerImage()
        delay()
    }
}
```

* showLoadingSpinner이 끝나지 않기 때문에(blocking call로 만들어져 있음) uploadService.upload(image)는 수행되지 않는다.

### 블럭킹 콜

* 작업이 완료할때까지 끝나지 않는 함수를 blocking call이라고 부른다.
* 위의 showLoadingSpinner() 함수는 메인 스레드(UI thread)에서 실행되고 running이 false가 되기 전까지는 완료하지 않는다.
* 애니메이션은 UI thread에서 처리하는게 맞지만 업로드 작업은 UI 작업이 아니므로 긴작업을 수행할 수 있는 새로운 스레드가 필요하고 멀티 스레딩 매커니즘으로 해결할 수 있다.

### 메인 스레드와 워커 스레드

* 메인 스레드 혹은 UI 스레드는 UI를 관리하는 책임을 지니고 모든 응용프로그램은 데드락을 피하기 위해 하나의 메인 스레드만을 가진다.
* 그런이유로 백그라운드 혹은 워커 스레드에서는 UI를 처리할 수 없다.
* 여러개의 스레드 처리, 즉 멀티 스레딩 작업에서는 동시성 테크닉을 통해 여러개의 스레드를 컨트롤 한다.
* 워커 스레드의 작업 결과를 메인 스레드에서 사용하기 위해서는 둘 간의 커뮤니케이션이 필요하다.

### 백그라운드와 UI 스레드 상호작용

* spinner 이미지 회전, 이미지를 업로드 완료 통지를 메인 스레드에 전달해 회전된 이미지를 다시그리거나, 업로드 완료여부를 표시하는 일련의 동시성 작업들이 블럭킹 없이 잘 동작하기 위해서는 두 스레드간에 상호작용이 필요하다.

### 데이터 공유

* 다른 두 스레드간 상호작용을 하기 위해서는 데이터를 공유해야 한다.
* 두 스레드간 안전하게 데이터를 처리하기 위해서는 동기화라는 작업이 필요하다.
* 특별한 케이스로 읽기만 하고 업데이트를 할 수 없게 만든다면 이는 스레드에 안전하고 이러한 데이터를 immutable 자료형 이라고 부른다.

### 큐

* 보통 스레드간 통신에서는 생산자/소비자 패턴을 가지는 큐를 사용한다.
* 생산자 스레드에서는 정보(메시지)를 넣기만 하고 소비자 스레드에서는 이를 가져와 처리하는 역할을 담당한다.
* 소비자 스레드에서 새로운 메시지가 올때까지 대기하도록 동기화 기능을 제공해주는 블러킹 큐도 있다.

### 콜백을 사용해 작업 완료 처리

* 콜백은 비동기 프로그래밍 매커니즘에서 자주 사용되어 진다.

```kotlin
interface OnUploadCallback {
    fun onUploadCompleted()
}
```

* 오래 걸리는 작업이 완료되었을때 콜백을 실행하게 되고 코틀린에서는 람다 표현으로 사용할 수 있다.

```kotlin
fun uploadImage(image: Image) {
    showLoadingSpinner()
    uploadService.upload(image) { hideLoadingSpinner() }
}
```

### Indentation hell

* 콜백은 스레드간 통신에 사용되는 심플한 방법이지만 여러 함수 중첩 콜백 사용시 많은 {}가 사용되어져 Depth가 깊어져 코드를 읽고 관리하는데 어려움을 겪을 수 있다.

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
* Rx에는 스트림 데이터를 깔끔하게 처리하도록 많은 수의 연산자를 제공한다.

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
* 특히 에러 발생시 에러 또한 스트림으로 오기 때문에 onError lambda에서 처리한 모습을 볼 수 있다. 만일 콜백이었다면 try/catch를 사용해야 했을 것이다.

### 코루틴 탐험

* 코루틴의 중요 컨셉은 이미 과거에 조명되었던 것으로 suspension points, suspendable functions and continuations 일급객체 언어에 대한것이다.

```kotlin
fun fetchUser(userId: String) {
    val user = userService.getUser(userId) // 1

    print("Fetching user") // 2
    print(user.name) // 3
    print("Fetched user") // 4
}
```

* 위 코드가 정상동작하기 위해서는 1은 3보다 무조건 먼저 실행되어져야 한다.
* 코루틴은 위의 방식대로 동작하게 해주고 이를 part-thread, part-callback 매커니즘이라고 부른다.
* getUser()가 suspendable function으로 마크되어 있다면 시스템은 이 함수를 백그라운드로 실행시킨다.
* 그 결과를 사용할 위치(user.name)에서 준비가 완료될때까지 기다리게 되고 이를 awaiting이라고 부른다.
* 중요한 점은 getUser() 함수를 부른 후에 멈추지 않는다는 것이다.

### 키포인트

* 멀티스레딩은 동시에 여러개의 일을 실행할 수 있다.
* 비동기 프로그래밍은 스레드간 통신 중 일반적인 패턴이다.
* 스레드간 데이터를 공유하는 매커니즘에는 여러 종류가 있는데 그중에는 큐, 파이프라인이 있다.
* 콜백은 복잡하고 유지보수가 어려운 매커니즘이다.
* Rx는 데이터 전송, 조합, 에러핸들링에 클린한 방법을 제공해주지만 모든 애플리케이션에 적합한것은 아니다.
* 코루틴은 로우레벨 스케줄링에 기초를 둔 안정적인 컨셉이다.
* 코루틴은 항상 새로운 스레드를 만들지 않고 스레드 풀 이미 있는 스레드를 재사용한다.
* 코루틴을 통해 순차적인 스타일로 비동기 코드를 작성할 수 있다.

## `1.2. 빌드환경 세팅`

* 

## `1.3. 코루틴 시작`

* 

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