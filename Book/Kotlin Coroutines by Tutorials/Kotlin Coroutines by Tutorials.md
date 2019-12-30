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

### Blocking Call

* 작업이 완료할때까지 끝나지 않는 함수를 blocking call이라고 부른다.
* 위의 showLoadingSpinner() 함수는 main thread(UI thread)에서 실행되고 running가 false가 되기 전까지는 완료하지 않는다.
* 애니메이션은 UI thread에서 처리하는게 맞지만 업로드 작업은 UI 작업이 아니므로 긴작업을 수행할 수 있는 a new thread가 필요하고 multi-threading 매커니즘으로 해결할 수 있다.

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