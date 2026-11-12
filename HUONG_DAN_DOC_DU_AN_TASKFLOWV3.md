# Hướng dẫn đọc và hiểu TaskFlowV3 từ đầu đến cuối

> Dành cho người đã biết Java, Spring Boot, REST API, JPA và Dependency Injection, đang học Android Kotlin.
>
> Đối chiếu source ngày 12/11/2026. Package thực tế: com.example.taskflowtraining. Database hiện tại: version 2.
>
> Đây là tài liệu học và đọc code, không phải tuyên bố mọi tính năng đã hoàn chỉnh. Những hạn chế của source hiện tại được ghi rõ. Lượt tạo tài liệu không chạy build, test hoặc ứng dụng và không chỉnh sửa code.
>
> Đường dẫn trong tài liệu tính từ thư mục TaskFlowV3. Các link tương đối có thể mở trong IDE/Markdown preview. Khi code thay đổi, ưu tiên nội dung source thực tế hơn mô tả trong tài liệu.

## Mục lục

1. [Cách sử dụng tài liệu](#1-cách-sử-dụng-tài-liệu)
2. [Bản đồ hệ thống](#2-bản-đồ-hệ-thống)
3. [Cây thư mục và trách nhiệm](#3-cây-thư-mục-và-trách-nhiệm)
4. [Kotlin cần hiểu trước khi đọc](#4-kotlin-cần-hiểu-trước-khi-đọc)
5. [Ứng dụng khởi động như thế nào](#5-ứng-dụng-khởi-động-như-thế-nào)
6. [Đọc XML và ViewBinding](#6-đọc-xml-và-viewbinding)
7. [Đọc màn danh sách](#7-đọc-màn-danh-sách)
8. [Đọc RecyclerView và Adapter](#8-đọc-recyclerview-và-adapter)
9. [Đọc TaskViewModel và UI State](#9-đọc-taskviewmodel-và-ui-state)
10. [Coroutines, Flow và lifecycle](#10-coroutines-flow-và-lifecycle)
11. [Model, Entity, DTO và mapper](#11-model-entity-dto-và-mapper)
12. [Repository và từng thao tác CRUD](#12-repository-và-từng-thao-tác-crud)
13. [Room và SQLite](#13-room-và-sqlite)
14. [Hilt và dependency graph](#14-hilt-và-dependency-graph)
15. [Navigation và SavedStateHandle](#15-navigation-và-savedstatehandle)
16. [Màn Detail](#16-màn-detail)
17. [Màn Edit và lưu Task](#17-màn-edit-và-lưu-task)
18. [DataStore và sort](#18-datastore-và-sort)
19. [Retrofit và import mẫu](#19-retrofit-và-import-mẫu)
20. [WorkManager từ enqueue đến kết quả](#20-workmanager-từ-enqueue-đến-kết-quả)
21. [Notification và Photo Picker](#21-notification-và-photo-picker)
22. [Localization và UiText](#22-localization-và-uitext)
23. [Compose thực hành](#23-compose-thực-hành)
24. [Gradle, compiler và dependency](#24-gradle-compiler-và-dependency)
25. [Đọc test](#25-đọc-test)
26. [Migration 1→2 và schema JSON](#26-migration-12-và-schema-json)
27. [Lỗi Serialization đã gặp](#27-lỗi-serialization-đã-gặp)
28. [State sống ở đâu và trong bao lâu](#28-state-sống-ở-đâu-và-trong-bao-lâu)
29. [Những hạn chế cần nhận biết](#29-những-hạn-chế-cần-nhận-biết)
30. [Tra cứu theo triệu chứng](#30-tra-cứu-theo-triệu-chứng)
31. [Kế hoạch tự đọc và bài tập](#31-kế-hoạch-tự-đọc-và-bài-tập)
32. [Câu hỏi tổng kết và đáp án](#32-câu-hỏi-tổng-kết-và-đáp-án)
33. [Từ điển nhanh và tài liệu tham khảo](#33-từ-điển-nhanh-và-tài-liệu-tham-khảo)

## 1. Cách sử dụng tài liệu

### 1.1. Đừng đọc từng file từ trên xuống một cách độc lập

Một thao tác thêm Task đi qua nhiều lớp. Một file lớn như ListFragment còn chứa nhiều tính năng. Nếu đọc hết Fragment trước khi biết Repository là gì, bạn sẽ dễ mất dấu luồng.

Mỗi lượt đọc hãy chọn đúng một tình huống, ví dụ:

- Mở app lần đầu.
- Thêm một Task.
- Tích checkbox hoàn thành.
- Nhấn title để xem chi tiết.
- Sửa và nhấn Lưu.
- Import mẫu khi mất mạng.
- Nâng database v1 lên v2.

Sau đó đọc cả chiều hành động lẫn chiều kết quả quay lại UI.

### 1.2. Sáu câu hỏi áp dụng cho mọi hàm

1. Ai gọi hàm này?
2. Nó nhận dữ liệu gì?
3. Nó trả kết quả một lần, trả Flow, hay chỉ thay đổi state?
4. Ai sở hữu vòng đời của tác vụ?
5. Khi lỗi hoặc bị hủy thì xảy ra gì?
6. UI biết kết quả bằng cách nào?

Ghi chú theo mẫu:

| Hàm | Caller | Input | Output/state | Lỗi/cancellation | Lifetime |
|---|---|---|---|---|---|
| TaskViewModel.add | ListFragment | title, priority | busy/error/message và thay đổi Room | runAction xử lý | ViewModel |
| TaskDao.observeAll | Repository | Không | Flow danh sách Entity | upstream có thể lỗi | Theo collector |
| RefreshWorker.doWork | WorkManager | WorkerParameters + Repository | Result | phân loại retry/failure | WorkManager |

### 1.3. Phân biệt ba mức kết luận

- **Source cho thấy:** có thể kết luận từ implementation.
- **Đã kiểm chứng:** có kết quả chạy cụ thể, chỉ trong phạm vi đó.
- **Chưa xác minh:** mới suy luận, chưa có bằng chứng runtime.

Trong phiên trước, assembleDebug và migration test đã PASS. Không được suy ra toàn bộ unit/instrumented tests đều PASS hoặc mọi cấu hình lifecycle đều đúng.

### 1.4. Không tự sửa trong lúc chưa hiểu

Trước mỗi sửa đổi, thử mô tả luồng hiện tại bằng lời. Nếu chưa giải thích được caller và owner của state, hãy đọc tiếp thay vì tạo abstraction hoặc chuyển package.

Các checklist ở cuối từng chương là bài tập tự đọc. Không cần thực hiện tất cả thay đổi được nhắc như khả năng cải thiện.

## 2. Bản đồ hệ thống

### 2.1. Hai chiều dữ liệu

~~~text
Hành động:
Người dùng → Fragment → ViewModel → Repository → Room / Retrofit

Hiển thị:
Room → DAO Flow → Repository mapping → ViewModel UI State
     → Fragment render → Views / RecyclerView
~~~

Luồng import hiện tại có một nhánh nền:

~~~text
btnImport → TaskViewModel.importSample → WorkManager.enqueueUniqueWork
          → RefreshWorker.doWork → Repository.importSample
          → Retrofit → mapping → Room

WorkManager → WorkInfo → ViewModel → Fragment hiển thị tiến trình/kết quả
Room        → Flow dữ liệu            → Fragment cập nhật danh sách
~~~

Hai nhánh kết quả này độc lập: WorkInfo cho biết công việc đang ở trạng thái nào; Room cho biết dữ liệu nào đang tồn tại.

### 2.2. Đối chiếu Spring Boot

| Android trong dự án | Liên hệ Java/Spring | Giới hạn phép so sánh |
|---|---|---|
| Hilt | DI container | Component Android có lifecycle riêng, không giống mọi bean cùng sống trong server |
| Fragment | Lớp trình bày, nhận tương tác | Không phải REST Controller; View có thể được tạo/hủy nhiều lần |
| ViewModel | Điều phối hành động và state màn hình | Không phải Spring Service, phải nghĩ tới lifecycle và khôi phục state |
| Repository interface | Hợp đồng truy cập dữ liệu | Source hiện còn validation và phối hợp local/remote |
| TaskDao | Lớp truy vấn dữ liệu | Room sinh implementation từ annotation |
| TaskEntity | Persistence model tương tự JPA Entity | Không có managed entity/dirty checking như Hibernate |
| Retrofit API | HTTP client interface | Retrofit sinh implementation; không phải server endpoint |
| Flow | Luồng giá trị theo thời gian | Không chỉ là một List hoặc một Future |
| WorkManager | Job scheduler bền vững | Không đảm bảo chạy đúng một thời điểm tuyệt đối |
| Migration | DDL evolution | Kotlin default không tự sửa các hàng cũ |

### 2.3. Source of truth

Danh sách Task được hiển thị từ Room. Response Retrofit không được submit trực tiếp cho Adapter. Import ghi vào Room rồi Flow đưa dữ liệu lên.

Search/filter/priority filter là trạng thái lựa chọn trong TaskViewModel. Sort được lưu bằng DataStore. Không có cơ chế đồng bộ hai chiều với server; thao tác sửa/xóa Task hiện là local.

## 3. Cây thư mục và trách nhiệm

~~~text
TaskFlowV3/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml
│   ├── gradle-daemon-jvm.properties
│   └── wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle.kts
    ├── schemas/com.example.taskflowtraining.data.local.TaskDatabase/
    │   ├── 1.json
    │   └── 2.json
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/example/taskflowtraining/
        │   │   ├── TaskFlowApp.kt
        │   │   ├── MainActivity.kt
        │   │   ├── model/Task.kt
        │   │   ├── data/
        │   │   │   ├── TaskRepository.kt
        │   │   │   ├── Mappers.kt
        │   │   │   ├── local/TaskDatabase.kt
        │   │   │   ├── local/TaskMigrations.kt
        │   │   │   ├── remote/TodoApi.kt
        │   │   │   └── worker/RefreshWorker.kt
        │   │   ├── settings/
        │   │   ├── di/
        │   │   ├── notification/TaskNotificationHelper.kt
        │   │   ├── composepractice/TaskCounter.kt
        │   │   └── ui/
        │   │       ├── ListFragment.kt / TaskViewModel.kt / TaskAdapter.kt
        │   │       ├── DetailFragment.kt / DetailViewModel.kt
        │   │       ├── EditFragment.kt / EditViewModel.kt
        │   │       └── UiText.kt
        │   └── res/
        │       ├── layout/
        │       ├── navigation/nav_graph.xml
        │       ├── values/strings.xml
        │       ├── values/arrays.xml
        │       ├── values-vi/strings.xml
        │       └── drawable/...
        ├── test/java/...              Local unit tests
        └── androidTest/java/...       Android instrumented tests
~~~

Cấu trúc hiện tại chủ yếu theo layer. Các màn vẫn tập trung trong ui, chưa chia theo feature.

Kiến trúc mục tiêu có domain/model, domain/repository, data/repository và feature/tasks/... có thể là hướng cải tiến sau. Hiện tại không cần di chuyển file để hiểu luồng hoặc làm migration.

Một file Kotlin có thể chứa nhiều class/interface. TaskDatabase.kt chứa TaskEntity, TaskDao và TaskDatabase; TaskRepository.kt chứa interface và implementation. Đừng suy luận nội dung chỉ từ tên file.

Các file trong app/build/generated là mã sinh. Có thể đọc để hiểu Room/Hilt tạo gì, không sửa tay và không xem chúng là nguồn implementation do bạn quản lý.

## 4. Kotlin cần hiểu trước khi đọc

### 4.1. val, var và data class

val ngăn gán lại tham chiếu; không tự biến mọi object được tham chiếu thành immutable.

Task và TaskEntity là data class. Kotlin sinh equals/hashCode/toString/copy. Adapter dùng equality này để so sánh nội dung.

~~~kotlin
old.copy(isDone = done)
~~~

Biểu thức tạo object mới, giữ các thuộc tính khác. Nó không mutate old và không tự lưu database. Phải gọi dao.update để ghi.

### 4.2. Nullable và Elvis

~~~kotlin
val old = dao.findById(id) ?: return
~~~

Nếu không tìm thấy thì hàm kết thúc im lặng.

~~~kotlin
val old = dao.findById(id)
    ?: throw IllegalArgumentException("...")
~~~

Nếu không tìm thấy thì caller nhận exception.

Hai hành vi đều có trong Repository hiện tại: setDone/delete không báo lỗi ID thiếu; updateTitle có báo lỗi.

### 4.3. Lambda và function type

TaskAdapter nhận callback:

~~~kotlin
onDone: (Task, Boolean) -> Unit
onTaskClick: (Task) -> Unit
~~~

Adapter chỉ phát ý định. Fragment quyết định gọi ViewModel, mở dialog hoặc điều hướng. Gần với truyền một functional interface/Consumer trong Java, nhưng cú pháp Kotlin gọn hơn.

runAction nhận suspend lambda: phần try/catch và loading dùng chung, phần action khác nhau.

### 4.4. by viewModels, by remember và by preferencesDataStore

Từ khóa by là delegation, nhưng từng delegate có ý nghĩa riêng:

- by viewModels(): lấy ViewModel từ ViewModelStore của owner.
- var count by remember { mutableIntStateOf(0) }: đọc/ghi Compose state qua delegate.
- Context.taskflowDataStore by preferencesDataStore(...): delegate cung cấp DataStore.

Không xem cả ba là cú pháp tạo singleton giống nhau.

### 4.5. Extension function

TaskEntity.toModel(), TodoDto.toEntity(), Throwable.toUiText() là extension functions.

Chúng không thêm phương thức thật vào class của thư viện; compiler hỗ trợ cách gọi dạng receiver. Một mapper ở data không có nghĩa Entity tự biết về UI.

### 4.6. Sealed interface và when

DetailUiState, EditUiState, EditSaveState mô hình hóa một tập trạng thái hữu hạn.

Ưu điểm: khi when bao phủ đủ các nhánh, compiler giúp bạn nhận ra nhánh còn thiếu. Tránh tổ hợp mâu thuẫn như vừa Content vừa NotFound trong cùng một state.

### 4.7. apply, with, let, use

| Hàm | Cách đọc trong source |
|---|---|
| apply | Cấu hình object rồi trả chính object đó |
| with(binding) | Truy cập View của binding ngắn gọn |
| let | Dùng giá trị không-null hoặc chuyển đổi giá trị |
| use | Đóng tài nguyên Closeable sau block, kể cả khi lỗi |

Cursor trong migration test và InputStream trong checkImageUri cần đóng; use thể hiện việc quản lý tài nguyên đó.

**Tự kiểm tra:** giải thích vì sao copy không giống Hibernate dirty checking; vì sao ?.let không chạy khi giá trị null.

## 5. Ứng dụng khởi động như thế nào

Đọc theo thứ tự:

1. [AndroidManifest.xml](app/src/main/AndroidManifest.xml)
2. [TaskFlowApp.kt](app/src/main/java/com/example/taskflowtraining/TaskFlowApp.kt)
3. [MainActivity.kt](app/src/main/java/com/example/taskflowtraining/MainActivity.kt)
4. [activity_main.xml](app/src/main/res/layout/activity_main.xml)
5. [nav_graph.xml](app/src/main/res/navigation/nav_graph.xml)

### 5.1. Manifest

android:name=".TaskFlowApp" chỉ định Application class. MainActivity có MAIN/LAUNCHER nên là entry point khi mở biểu tượng ứng dụng.

INTERNET cho phép truy cập mạng. POST_NOTIFICATIONS là khai báo quyền; khai báo một mình không đồng nghĩa người dùng đã cấp runtime permission.

Provider App Startup vẫn được merge, nhưng metadata WorkManagerInitializer bị remove. Mục đích là cho WorkManager sử dụng cấu hình custom có HiltWorkerFactory.

### 5.2. Application

TaskFlowApp có @HiltAndroidApp và triển khai Configuration.Provider.

Có hai nhiệm vụ đáng đọc riêng:

- Cung cấp workManagerConfiguration sử dụng workerFactory.
- Tạo notification channel task_reminders_high trong onCreate.

Application không phải nơi chứa danh sách Task dùng chung giữa màn hình.

Property getter xây Configuration khi được hỏi; không phải mỗi lần gọi getter là chạy Worker.

### 5.3. Activity

MainActivity inflate ActivityMainBinding rồi setContentView(binding.root). Không có danh sách Task, API call hoặc xử lý CRUD.

@AndroidEntryPoint cho phép Hilt tích hợp entry point này.

### 5.4. Ai mở ListFragment?

Root layout là FragmentContainerView có android:name là NavHostFragment. Nó tham chiếu nav_graph qua app:navGraph. Graph có startDestination là listFragment.

~~~text
Launcher → MainActivity → ActivityMainBinding.root
         → NavHostFragment → nav_graph → listFragment
~~~

MainActivity không cần tự new ListFragment hoặc gọi fragment transaction vì NavHost quản lý việc đó.

**Tự kiểm tra:** chỉ ra sự khác nhau giữa ID nav_host_fragment, ID nav_graph và ID listFragment.

## 6. Đọc XML và ViewBinding

### 6.1. Mapping tên file sang binding

| XML | Binding |
|---|---|
| activity_main.xml | ActivityMainBinding |
| fragment_list.xml | FragmentListBinding |
| fragment_detail.xml | FragmentDetailBinding |
| fragment_edit.xml | FragmentEditBinding |
| item_task.xml | ItemTaskBinding |

Tên ID etTitle thành property etTitle. Lỗi unresolved binding property thường cần đối chiếu XML ID và mã sinh, không vội đổi dependency.

### 6.2. Inflate khác bind

- inflate tạo View hierarchy từ XML.
- bind nhận View đã tồn tại và tìm các View theo ID.

ListFragment override onCreateView và inflate binding, trả binding.root.

Detail/Edit truyền layout cho Fragment constructor rồi bind(view) trong onViewCreated. Fragment base tạo View từ layout trước, sau đó callback nhận View.

Lỗi màn trắng đã từng xuất hiện khi DetailFragment kế thừa Fragment() nhưng không override onCreateView: không có View thì onViewCreated không chạy. bind không thể thay thế inflate trong trường hợp này.

### 6.3. Fragment lifecycle khác View lifecycle

Một Fragment trên back stack có thể còn sống khi View cũ đã bị hủy.

_binding là nullable vì có thời điểm không tồn tại View. Getter requireNotNull chỉ thuận tiện trong khoảng View hợp lệ; nó không ngăn truy cập sai lifecycle.

onDestroyView phải bỏ binding. ListFragment còn tháo RecyclerView.adapter để cắt liên kết View cũ với adapter.

### 6.4. Cách đọc layout

Mở fragment_list.xml và tìm theo ID thay vì đọc mọi thuộc tính:

- etTitle, spinnerPriority, btnAdd: vùng thêm.
- etSearch, spinnerFilter, spinnerSort, spinnerPriorityFilter: vùng lọc.
- btnImport, btnClear: thao tác nhóm.
- progressBar, tvFeedback, tvEmpty: state.
- recyclerTasks: danh sách.

Trong LinearLayout dọc, RecyclerView dùng height 0dp + weight 1 để nhận phần chiều cao còn lại. Đây không có nghĩa danh sách cao 0px ở layout cuối.

Đọc cả values/arrays.xml: thứ tự priority_options là low=1, medium=2, high=3. Phép selectedItemPosition + 1 phụ thuộc thứ tự này.

**Tự kiểm tra:** nếu đổi thứ tự array mà không đổi mapping Kotlin, dữ liệu nào sẽ sai?

## 7. Đọc màn danh sách

File: [ListFragment.kt](app/src/main/java/com/example/taskflowtraining/ui/ListFragment.kt).

Đọc theo trình tự sau, không cần đọc 540 dòng cùng lúc:

### 7.1. Các field

- _binding/binding: View của màn.
- vm by viewModels(): ViewModel scope theo Fragment.
- adapter: phát callback thao tác từng hàng.
- currentState: bản state gần nhất để renderImportState biết loading CRUD.
- renderingControls: chặn listener coi cập nhật bằng code là thao tác mới của người dùng.

currentState không phải một database hoặc danh sách Task độc lập dùng làm source of truth. Nó là snapshot UI state.

### 7.2. onViewCreated

setupList → setupInputs → setupSpinners → observeState.

Đây là nơi nối View với hành vi. Các hàm vẫn có thể đăng ký callback chạy về sau; thứ tự gọi không có nghĩa mọi công việc bất đồng bộ đã hoàn tất.

### 7.3. setupInputs

btnAdd:

1. Đọc chuỗi từ etTitle.
2. Chặn blank tại UI.
3. Tính priority.
4. Gọi vm.add.
5. Xóa ô nhập ngay.

Điểm cần biết: xóa ô nhập không đợi Room ghi thành công. Nếu lưu lỗi, nội dung vừa nhập đã bị clear. Đây là hành vi hiện tại, không phải bảo đảm UX tối ưu.

btnImport chỉ gọi vm.importSample; hiện method này enqueue WorkManager, không trực tiếp gọi API.

btnClear mở xác nhận rồi gọi vm.clearCompleted.

tvFeedback cho phép dismiss message/error trong ViewModel.

### 7.4. setupSpinners và vòng phản hồi

Nếu render gọi spinner.setSelection, listener có thể được kích hoạt. Nếu listener lại gọi setFilter/setSort, có thể tạo cập nhật thừa.

renderingControls được dùng để phân biệt thao tác render với thao tác người dùng. Đồng thời source khôi phục selection từ vm.uiState.value trước khi gắn listener.

Đừng mặc định guard bao phủ mọi callback đến muộn của Spinner. Back/rotation và restore state là tình huống cần test thực tế.

### 7.5. observeState

Có ba child coroutine trong repeatOnLifecycle:

- Collect uiState → render.
- Collect importWorkInfo → renderImportState.
- Collect importResultToShow → Toast → acknowledge.

Phải launch riêng: collect StateFlow thường không tự kết thúc, nên collect thứ nhất đặt tuần tự sẽ chặn đoạn collect tiếp theo.

### 7.6. render

Đọc theo các nhóm:

1. Lưu currentState.
2. submitList(state.tasks).
3. Loading và enable/disable buttons.
4. Empty state.
5. Feedback qua UiText.resolve.
6. Đồng bộ filters và query trong renderingControls.

Không có dữ liệu hợp filter khác với database không có dữ liệu. tvEmpty biểu thị danh sách kết quả đang rỗng, không khẳng định bảng tasks rỗng.

### 7.7. Hai cách sửa Task hiện cùng tồn tại

- btnEdit trên hàng → showEditDialog → vm.updateTask.
- Nhấn title → Detail → EditFragment → EditViewModel.saveTask.

Đừng nhầm dialog cũ đã bị xóa khi có EditFragment. Khi debug sửa title, phải xác định đang dùng đường nào.

**Bài tập:** vẽ riêng luồng thêm, xóa một Task và xóa completed; đánh dấu đoạn cần người dùng xác nhận.

## 8. Đọc RecyclerView và Adapter

File: [TaskAdapter.kt](app/src/main/java/com/example/taskflowtraining/ui/TaskAdapter.kt).

### 8.1. RecyclerView tái sử dụng View

Một holder có thể từng hiển thị Task A, sau đó được bind cho Task B. Vì vậy onBindViewHolder phải đặt lại dữ liệu và listener dựa trên Task hiện tại.

onCreateViewHolder chỉ tạo View hàng. onBindViewHolder đưa dữ liệu vào hàng.

### 8.2. ListAdapter và DiffUtil

areItemsTheSame so id: cùng thực thể logic không?

areContentsTheSame so old == new: nội dung data class có bằng nhau không?

Một Task cùng ID nhưng isDone khác: cùng item, khác content. DiffUtil giúp cập nhật hàng thích hợp.

Không sửa mutable list đã submit rồi kỳ vọng diff luôn nhận ra. Source hiện dùng danh sách/mapping mới theo Flow.

### 8.3. Checkbox listener

Source gỡ listener trước khi gán isChecked rồi mới gắn lại.

Nếu không gỡ, việc bind lại trạng thái checkbox có thể gọi onDone như người dùng vừa bấm, gây ghi database không cần thiết.

### 8.4. Adapter không tự làm CRUD

Adapter không biết DAO/Repository/NavController. Nó chỉ gọi các callback được Fragment truyền vào. Đây là ranh giới giúp tái sử dụng và kiểm thử dễ hơn.

**Tự kiểm tra:** tại sao dùng title để nhận dạng item sẽ sai khi hai Task trùng tên?

## 9. Đọc TaskViewModel và UI State

File: [TaskViewModel.kt](app/src/main/java/com/example/taskflowtraining/ui/TaskViewModel.kt).

### 9.1. Constructor

repo: truy cập Task.

settings: đọc/ghi sort.

workManager: enqueue/quan sát công việc nền.

Hilt tạo ViewModel. Fragment không tự khởi tạo DAO, Retrofit hoặc WorkManager để truyền tay.

### 9.2. Mutable state nội bộ

query, filter, priorityFilter: lựa chọn của màn hình.

busy, error, message: tiến trình và phản hồi của runAction.

selectedImportId, acknowledgedImportId: chọn/đánh dấu kết quả WorkManager trong bộ nhớ ViewModel.

UI nhận StateFlow, không được trực tiếp sửa các MutableStateFlow private này.

### 9.3. Hai tầng combine

Tầng một:

~~~text
repo.observeTasks + query + filter + settings.sortMode + priorityFilter
  → filterAndSort(...)
  → ListState
~~~

Tầng hai:

~~~text
ListState + busy + error + message
  → TaskUiState
~~~

Mỗi khi một nguồn phát giá trị mới, combine sử dụng các giá trị gần nhất của những nguồn còn lại sau khi các nguồn đã có emission ban đầu.

Vì sao tách tầng? Dễ gom thông tin danh sách trước rồi bổ sung loading/feedback. Không phải yêu cầu bắt buộc của MVVM.

priorityFilter phải được truyền từ ListState sang TaskUiState; nếu bỏ quên, danh sách và Spinner sẽ mô tả hai lựa chọn khác nhau. Lỗi này đã từng có và source hiện đã truyền đủ.

### 9.4. stateIn

stateIn biến Flow thành StateFlow chia sẻ:

- scope: viewModelScope.
- started: WhileSubscribed(5_000).
- initialValue: TaskUiState().

StateFlow giữ giá trị gần nhất. value có thể đọc đồng bộ, nhưng ban đầu có thể chỉ là default state trước khi Room/DataStore phát.

5_000 là thời gian trì hoãn dừng upstream sau khi không còn subscriber, không phải chu kỳ refresh 5 giây. Giá trị replay mặc định không bị xóa chỉ vì hết 5 giây.

### 9.5. runAction

Luồng:

~~~text
launch → chờ Mutex → busy=true → clear feedback
       → action
       → message thành công nếu có
       → catch lỗi
       → finally busy=false
~~~

Mutex ở ViewModel tuần tự hóa các action chạy qua nó. Nó không khóa mọi thao tác database toàn ứng dụng: EditViewModel và Worker có thể hoạt động ngoài Mutex này.

Nếu một action đợi Mutex thì busy của action đó chưa được set trong lúc đợi. Đây là lý do phải đọc cả vị trí withLock lẫn finally.

HttpException giữ HTTP code trong UiText; IOException thành lỗi mạng/lưu trữ; exception khác qua toUiText.

CancellationException phải ném lại để structured concurrency hoạt động. Không xem việc ViewModel bị clear là một lỗi nghiệp vụ cần Toast.

### 9.6. filterAndSort thực sự làm gì?

Trong model/Task.kt:

1. Trim query.
2. Title contains query, ignoreCase=true.
3. Kiểm tra priority nếu có.
4. Kiểm tra ALL/ACTIVE/COMPLETED.
5. NEWEST: id giảm dần.
6. TITLE: lowercase title rồi id tăng dần khi title bằng nhau.

NEWEST hiện là thứ tự ID, không phải timestamp vì Task không có createdAt.

contains(ignoreCase=true) không đồng nghĩa tìm kiếm bỏ dấu tiếng Việt. lowercasing + so sánh chuỗi cũng không phải thuật toán sắp xếp ngôn ngữ đầy đủ.

**Bài tập:** dự đoán kết quả với ba Task cùng title khác hoa/thường và khác ID; đối chiếu TaskFilterAndSortTest.

## 10. Coroutines, Flow và lifecycle

### 10.1. suspend không tự tạo background thread

Một hàm suspend có thể tạm ngưng mà không chặn thread tại điểm suspension. Hàm vẫn có thể chứa code blocking nếu bạn viết như vậy.

Room suspend DAO và Retrofit suspend API hỗ trợ thực thi bất đồng bộ của chúng. Không cần bọc mọi lời gọi bằng IO chỉ vì có chữ database/network, nhưng phải hiểu API cụ thể.

Trong importSample của ViewModel, operation.result.get() và getWorkInfos...get() là lời gọi chờ Future; source đặt chúng trong withContext(Dispatchers.IO) để không chặn main thread.

### 10.2. Các owner khác nhau

| Scope/cơ chế | Owner | Khi nào phù hợp |
|---|---|---|
| viewModelScope | ViewModel | Action thuộc màn hình |
| viewLifecycleOwner.lifecycleScope | View lifecycle | Công việc gắn với Views |
| repeatOnLifecycle(STARTED) | Trạng thái lifecycle | Bật/tắt UI collection |
| WorkManager | Hệ thống lập lịch | Work cần tồn tại ngoài lifecycle màn hình |

Worker không phải coroutine dài hơn được cất trong Activity.

### 10.3. Cold Flow và StateFlow

DAO observeAll trả Flow được collect để quan sát dữ liệu. Repository dùng map để đổi Entity thành model.

StateFlow trong ViewModel giữ state gần nhất và phát cho subscriber mới. Nó có cơ chế hợp nhất theo equality; không phải queue lưu mọi thay đổi trung gian để mọi collector chắc chắn nhận đủ.

Vì vậy StateFlow phù hợp state hơn là cam kết xử lý mỗi event đúng một lần.

### 10.4. Lifecycle-safe collection

repeatOnLifecycle dừng block khi lifecycle xuống dưới STARTED và khởi động lại khi đủ điều kiện. Outer coroutine vẫn gắn với View lifecycle và bị hủy khi View bị destroy.

Ưu điểm: render không cố truy cập View cũ sau khi điều hướng làm mất View.

Không dùng Fragment.lifecycleScope một cách máy móc cho render View: Fragment có thể còn tồn tại sau onDestroyView.

### 10.5. catch và lỗi upstream

catch trong Detail/Edit chuyển lỗi tải thành Error state. Nó không tự làm nguồn dữ liệu phát lại sau lỗi.

TaskViewModel.runAction chỉ bắt lỗi trong action. Nó không bao quanh lỗi upstream của combine(repo.observeTasks(), settings.sortMode...). Đây là hạn chế khác cần nhận biết, không được gọi mọi exception đều đã xử lý.

**Tự kiểm tra:** vì sao rotate có thể giữ ViewModel nhưng vẫn phải gỡ binding cũ?

## 11. Model, Entity, DTO và mapper

Đọc:

- [Task.kt](app/src/main/java/com/example/taskflowtraining/model/Task.kt)
- [TaskDatabase.kt](app/src/main/java/com/example/taskflowtraining/data/local/TaskDatabase.kt)
- [TodoApi.kt](app/src/main/java/com/example/taskflowtraining/data/remote/TodoApi.kt)
- [Mappers.kt](app/src/main/java/com/example/taskflowtraining/data/Mappers.kt)

### 11.1. Ba biểu diễn dữ liệu

| Kiểu | Mục đích | Chi tiết đặc trưng |
|---|---|---|
| Task | Model dùng bởi UI/logic | id, title, isDone, priority, remoteId |
| TaskEntity | Hàng trong Room | Annotation, description và SQL default |
| TodoDto | Response server | userId, id, title, completed |

Không truyền Entity khắp UI chỉ vì thuộc tính trông giống model. Ranh giới giúp thay database/API mà hạn chế ảnh hưởng UI.

### 11.2. Hai loại ID

TaskEntity.id là Long local, autoGenerate.

TodoDto.id là Int từ server, được giữ trong remoteId.

Ví dụ một DTO có id=7 có thể được lưu thành hàng local id=31, remoteId=7. Navigation phải truyền local id=31 để DAO tìm đúng hàng.

### 11.3. Mapping hiện tại

TaskEntity.toModel giữ id/title/isDone/priority/remoteId.

TodoDto.toEntity:

- trim title, take(100).
- bỏ DTO có id <= 0 hoặc title trống.
- completed → isDone.
- dto.id → remoteId.
- priority và description dùng default Entity.

userId chưa được lưu.

### 11.4. description mới ở persistence

Entity v2 có description nhưng Task model và mapper chưa mang thuộc tính này. Do đó Detail/Edit chưa có chức năng đọc/sửa description.

Đây là phạm vi hiện tại của bài migration, không phải lỗi mất description khi updateTitle: implementation đọc Entity cũ rồi copy các trường cần sửa, nên description cũ được giữ lại.

**Bài tập:** tự viết bảng mapping DTO → Entity → Task, đánh dấu trường nào bị bỏ qua ở mỗi bước.

## 12. Repository và từng thao tác CRUD

File: [TaskRepository.kt](app/src/main/java/com/example/taskflowtraining/data/TaskRepository.kt).

Interface là hợp đồng; DefaultTaskRepository là implementation được AppModule cung cấp.

### 12.1. observeTasks và observeTaskById

observeTasks map từng Entity trong danh sách thành Task.

observeTaskById map nullable Entity thành nullable Task. null có ý nghĩa “không còn/tồn tại Task theo ID”, không đồng nghĩa lỗi network.

### 12.2. Thêm

~~~text
UI kiểm tra blank
→ ViewModel.add
→ Repository.add kiểm tra priority 1..3
→ validateTitle trim, nonblank, tối đa 100
→ tạo Entity
→ dao.insert
→ Room emit danh sách mới
~~~

UI validation phản hồi sớm; Repository/model validation bảo vệ các caller khác như Worker/test hoặc UI khác. Hai tầng không nhất thiết có cùng toàn bộ trách nhiệm.

### 12.3. Đánh dấu hoàn thành

setDone(id, done):

1. findById.
2. Không có thì return.
3. copy(isDone=done).
4. update.

Task mà callback giữ có thể là snapshot cũ, nhưng Repository chỉ dùng ID và đọc Entity mới trước khi update.

Dù vậy read rồi update là hai thao tác; không có transaction hoặc conditional UPDATE chuyên biệt. Một cập nhật đồng thời giữa hai bước có thể bị ghi đè bởi snapshot old.

### 12.4. Xóa

delete(id) đọc Entity rồi dao.delete. ID không có thì không làm gì.

UI yêu cầu xác nhận qua dialog, nhưng Repository không có khái niệm dialog; caller chịu trách nhiệm trải nghiệm đó.

### 12.5. Sửa

updateTitle(id, newTitle, newPriority) thực tế sửa cả title và priority dù tên method chỉ nói title.

ID thiếu → exception. Validate priority/title rồi copy/update Entity.

Không cần tạo mới Entity chỉ gồm id và title, vì làm vậy dễ reset các thuộc tính khác về default.

### 12.6. Xóa completed

clearCompleted gọi SQL deleteCompleted. Không duyệt list UI để xóa từng hàng, nên thao tác không chỉ giới hạn vào những hàng hiện đang hiển thị sau filter.

**Tự kiểm tra:** khi filter đang ACTIVE, nhấn clearCompleted có thể xóa gì? Đáp án: mọi hàng completed trong bảng, không phải chỉ kết quả filter.

## 13. Room và SQLite

### 13.1. Entity v2

| Cột | SQLite | Nullable | Ý nghĩa |
|---|---|---|---|
| id | INTEGER primary key autoincrement | Không | ID local |
| title | TEXT | Không | Tiêu đề |
| isDone | INTEGER | Không | Boolean được lưu dưới dạng số |
| priority | INTEGER | Không | Mức 1..3 theo validation ứng dụng |
| remoteId | INTEGER | Có | ID mẫu remote |
| description | TEXT DEFAULT '' | Không | Cột mới v2 |

SQLite không tự biết “priority 1..3” từ require của Kotlin. Hiện Entity không khai báo CHECK constraint cho phạm vi đó.

### 13.2. DAO

observeAll có ORDER BY id DESC. observeById/findById có WHERE id = :id LIMIT 1.

- :id là bind parameter, không phải nối chuỗi SQL thủ công.
- findById là snapshot một lần.
- observeById tiếp tục quan sát thay đổi trong quá trình collect.
- @Update dùng primary key để xác định hàng.
- @Delete xóa hàng tương ứng Entity.
- @Insert(IGNORE) không ghi đè hàng xung đột và trả -1L khi insert bị bỏ qua.

### 13.3. Unique index

Index unique trên remoteId đảm bảo remote ID không-null không trùng. SQLite cho phép nhiều hàng có NULL trong unique index, nên nhiều Task local không có remoteId vẫn được tạo.

Chống trùng remoteId không chống hai title giống nhau.

### 13.4. Database class và instance

TaskDatabase abstract khai báo taskDao(). Room sinh implementation để mở database, tạo bảng, validate schema và chạy DAO.

Instance thực tế được tạo trong AppModule qua Room.databaseBuilder với tên taskflow.db.

@Singleton là một instance trong Hilt component/process, không phải “chỉ một file database trên mọi thiết bị”.

### 13.5. Kotlin default khác SQL default

val description: String = "" áp dụng khi code Kotlin tạo object mà không truyền giá trị.

@ColumnInfo(defaultValue = "''") mô tả SQL DEFAULT '' trong schema Room.

ALTER TABLE ... DEFAULT '' xác định giá trị hợp lệ cho cột mới khi migrate các hàng cũ.

Kotlin default của isDone/priority không tự trở thành SQL default: đọc createSql trong JSON để kiểm chứng, không suy luận từ constructor.

### 13.6. Transaction

Không phải gọi nhiều suspend DAO trong cùng coroutine thì chúng tự thành một transaction. importSample hiện insert từng hàng, không có transaction bao toàn bộ vòng lặp.

**Bài tập:** giải thích chuyện gì còn lại trong database nếu insert thứ 8 gặp lỗi sau 7 insert thành công.

## 14. Hilt và dependency graph

File: [AppModule.kt](app/src/main/java/com/example/taskflowtraining/di/AppModule.kt).

### 14.1. Graph hiện tại

~~~text
ApplicationContext → TaskDatabase → TaskDao ─────────────┐
Retrofit configuration → TodoApi ───────────────────────┤
                                                       ↓
                                              TaskRepository
                                              ↙       ↓       ↘
                                     TaskViewModel DetailVM EditVM
                                              ↑
ApplicationContext → SettingsRepository ───────┤
ApplicationContext → WorkManager ──────────────┘

HiltWorkerFactory → RefreshWorker → TaskRepository
~~~

### 14.2. Annotation

| Annotation | Ý nghĩa tại dự án |
|---|---|
| @HiltAndroidApp | Sinh hạ tầng Hilt cho Application |
| @AndroidEntryPoint | Tích hợp Android component với Hilt |
| @HiltViewModel | Sinh factory hỗ trợ ViewModel constructor injection |
| @Inject constructor | Hilt biết cách tạo object từ dependency |
| @Module / @InstallIn | Khai báo bindings thuộc component |
| @Provides | Factory method cho dependency |
| @Singleton | Scope instance theo SingletonComponent |
| @ApplicationContext | Qualifier phân biệt loại Context |
| @HiltWorker / @AssistedInject | Kết hợp dependency Hilt và tham số do WorkManager cung cấp |

### 14.3. Đọc provider

provideDatabase: tạo database và đăng ký MIGRATION_1_2.

provideTodoApi: Retrofit base URL + Gson converter + create interface.

provideTaskRepository: nhận database/api, trả DefaultTaskRepository dưới kiểu interface.

provideSettingsRepository: DataStore repository dùng ApplicationContext.

provideWorkManager: getInstance(context), sử dụng Application configuration.

Provider DAO tồn tại, nhưng provider Repository đang gọi database.taskDao() trực tiếp. Không có nghĩa Hilt tự gọi provider DAO bên trong method này.

### 14.4. Hilt không xóa nhu cầu hiểu lifecycle

Không inject View vào singleton, không giữ Activity Context trong object sống suốt ứng dụng.

TaskNotificationHelper hiện được tạo thủ công trong DetailFragment với requireContext; không phải mọi object đều đã qua Hilt.

### 14.5. InstanceTracker

InstanceTracker có ID và singleton scope để quan sát/tập kiểm thử scope. Nó không phải repository dữ liệu hoặc service nghiệp vụ.

**Bài tập:** nếu constructor TaskViewModel cần TaskRepository interface, chỉ ra chính xác provider nào quyết định implementation.

## 15. Navigation và SavedStateHandle

### 15.1. Graph

~~~text
ListFragment
  └── action_list_to_detail(taskId: Long)
      → DetailFragment
          └── action_detail_to_edit(taskId: Long)
              → EditFragment
~~~

Graph khai báo destination class, ID, label và argument. Label không tự tạo Toolbar cho layout; title hiển thị còn phụ thuộc UI host.

### 15.2. Truyền ID

ListFragment.openTaskDetail dùng bundleOf("taskId" to task.id).

DetailFragment.openEdit truyền state.task.id.

Hai ViewModel dùng savedStateHandle.get<Long>("taskId"). String key phải đúng; kiểu Long phải khớp argType long.

Không có Safe Args plugin trong cấu hình hiện tại; source dùng Bundle và key string thủ công.

### 15.3. Vì sao chỉ truyền ID?

ID nhỏ, dễ khôi phục và dùng để đọc nguồn dữ liệu mới nhất.

Nếu truyền cả Entity, màn sau có thể giữ snapshot cũ. Khi Task bị xóa/sửa từ nơi khác, ID + Flow theo database giúp trạng thái phản ánh hiện tại.

### 15.4. Back stack

Với luồng thông thường, mở Edit rồi navigateUp quay về Detail. Detail đang observe Task theo ID nên nhận title/priority mới từ Room.

Không cần truyền lại cả Task khi quay về. Không mở thêm một ListFragment chỉ để “refresh” danh sách.

### 15.5. Scope và khôi phục

by viewModels() hiện scope ViewModel theo Fragment. Khi View danh sách được tạo lại trong khi back stack entry còn, ViewModel thường vẫn giữ state.

SavedStateHandle trong Detail/Edit hiện dùng cho argument taskId. Không có nghĩa mọi field của mọi ViewModel đã tự được lưu.

Thiếu argument gây error("Thiếu taskId"), khác với ID hợp lệ về kiểu nhưng không tìm thấy hàng: trường hợp sau dẫn tới NotFound.

**Bài tập:** giải thích khác nhau giữa taskId thiếu, taskId=999 không tồn tại và Task bị xóa sau khi mở Detail.

## 16. Màn Detail

Đọc [DetailViewModel.kt](app/src/main/java/com/example/taskflowtraining/ui/DetailViewModel.kt) trước, rồi [DetailFragment.kt](app/src/main/java/com/example/taskflowtraining/ui/DetailFragment.kt).

### 16.1. State machine

~~~text
Khởi tạo: Loading
DAO emit Task → Content(task)
DAO emit null → NotFound
Flow ném lỗi → Error(UiText)
~~~

Content và NotFound có thể chuyển qua lại khi dữ liệu database thay đổi; đây không phải chỉ kiểm tra ID một lần khi mở màn.

### 16.2. render

Ẩn các View state trước, rồi bật các View cần cho nhánh hiện tại. Content gán title, trạng thái hoàn thành và tên priority đã dịch.

btnBack luôn được thiết lập listener. btnEdit/btnReminder kiểm tra state là Content trước khi dùng Task.

### 16.3. Điều cần nhận biết

btnChoosePhoto và ivTaskPhoto không được quản lý hoàn toàn theo các nhánh Content/NotFound giống nhóm title/edit/reminder. Không suy ra ảnh đang gắn persistence với Task chỉ vì nằm trên màn Detail.

Đặt accessibilityHeading=true cho title giúp đánh dấu semantic heading; nó không thay đổi giá trị Task hoặc navigation.

**Tự kiểm tra:** nếu dao.observeById phát null sau một lần Content, render sẽ bật thông báo nào và ẩn nút nào?

## 17. Màn Edit và lưu Task

Đọc [EditViewModel.kt](app/src/main/java/com/example/taskflowtraining/ui/EditViewModel.kt) và [EditFragment.kt](app/src/main/java/com/example/taskflowtraining/ui/EditFragment.kt).

### 17.1. Hai nguồn state

EditUiState: loading dữ liệu hiện tại của Task.

EditSaveState: trạng thái yêu cầu lưu, gồm Idle/Saving/Success/Error.

Loading Task và Saving Task là hai công việc khác nhau; tách state giúp nhìn rõ điều đó, nhưng UI phải phối hợp chúng.

### 17.2. displayedTaskId

render Content chỉ điền etTitle/spinner nếu ID chưa được hiển thị. Mục đích là tránh mỗi emission của Room lại ghi đè những gì người dùng đang nhập.

Đổi title trong EditText chưa ghi database. Nó là dữ liệu đang nằm trong View.

onDestroyView reset displayedTaskId về null. Khi View được tạo lại, cơ chế điền dữ liệu và Android View state restoration có thể tương tác; chưa có quản lý draft chủ động để đảm bảo giữ nội dung mọi trường hợp.

### 17.3. Luồng lưu

~~~text
btnSave → trim title → position+1
→ EditViewModel.saveTask
→ kiểm tra Saving/Success
→ launch → Saving
→ repo.updateTitle(taskId, ...)
→ Success
→ collector của Fragment gọi navigateUp
~~~

Repository validation failure → Error(UiText) → hiển thị message và bật nút lưu.

Cancellation được ném lại.

### 17.4. Không kết luận quá mức từ guard

Guard kiểm tra state trước launch, còn set Saving nằm trong launch. Đây là guard trạng thái, không phải primitive atomicity hoặc transaction cho mọi concurrent caller.

Success giữ trong StateFlow và chưa có acknowledge/reset. Nếu collector bắt đầu lại trong khi ViewModel còn Success, nó có thể navigateUp lại.

render(EditUiState) có thể ẩn tvMessage mà saveState Error đang dùng; hai collector cùng tác động lên một View cần được nghĩ tới khi debug lỗi biến mất.

Nút save hiện enable theo saveState, chưa phối hợp đầy đủ NotFound/Loading của uiState. Nhận biết phạm vi này trước khi xem code là thiết kế hoàn chỉnh.

**Bài tập:** phân biệt dữ liệu Room, draft trong etTitle và saveState; giải thích cái nào thay đổi ngay khi gõ.

## 18. DataStore và sort

Đọc [SettingsRepository.kt](app/src/main/java/com/example/taskflowtraining/settings/SettingsRepository.kt) và [DataStoreSettingsRepository.kt](app/src/main/java/com/example/taskflowtraining/settings/DataStoreSettingsRepository.kt).

Context.taskflowDataStore có tên taskflow_settings. Key sort_mode là identifier kỹ thuật, không dịch theo locale.

Ghi:

~~~text
Spinner sort → TaskViewModel.setSort → settings.setSortMode
            → store.edit → preferences[sortKey] = mode.name
~~~

Đọc:

~~~text
store.data → đọc string → tìm enum name
           → không hợp lệ/thiếu thì NEWEST
           → combine trong TaskViewModel
~~~

Lưu enum.name giúp sort không phụ thuộc nhãn English/Vietnamese. Nếu sau này đổi tên enum, cần nghĩ tới dữ liệu preference đã lưu.

Không có query/filter/priorityFilter được lưu ở DataStore hiện tại. Chúng vẫn là MutableStateFlow trong bộ nhớ.

DataStore không thay Room cho danh sách Task. Đây là hai loại lưu trữ với mục đích khác nhau.

**Bài tập:** sau process recreation, sort và query có được khôi phục theo cùng cơ chế không?

## 19. Retrofit và import mẫu

### 19.1. API client

TodoApi.getTodos là GET todos. Base URL ở AppModule là https://jsonplaceholder.typicode.com/.

JSONPlaceholder là dịch vụ dữ liệu mẫu. Source này không có POST/PATCH/DELETE remote cho Task. Xóa Task local không xóa gì trên API.

### 19.2. importSample thực tế

Repository:

1. api.getTodos().
2. take(20).
3. Chuyển từng DTO thành nullable Entity.
4. Bỏ DTO không hợp lệ.
5. Insert IGNORE.
6. Đếm insert thành công.
7. Trả Int.

take(20) được áp dụng sau khi lấy List response, không phải server pagination.

Worker hiện bỏ qua Int trả về và chỉ trả Result.success(). UI hiển thị thông báo thành công tổng quát, không có tổng số insert xuyên suốt retry.

### 19.3. Retry và chống trùng

Với các remote ID ổn định và hàng vẫn còn trong Room, unique index + IGNORE khiến import lặp không tạo thêm hàng trùng remoteId.

Điều đó không đồng nghĩa “import chỉ được thực hiện một lần suốt đời”:

- Hàng đã xóa có thể được import lại.
- API đổi tập dữ liệu/ID có thể tạo hàng mới.
- Có thể có title trùng nhau với remoteId khác nhau.
- Hàng đã tồn tại không được cập nhật vì strategy là IGNORE.

### 19.4. Partial success

Không có transaction bao toàn bộ import. Nếu tiến trình dừng giữa vòng lặp, hàng đã insert vẫn còn.

Lượt tiếp theo bỏ qua hàng cũ và bổ sung phần còn thiếu. Tổng đếm của một lượt không bằng tổng số hàng đã thêm qua mọi attempt.

**Tự kiểm tra:** vì sao unique index là bảo vệ quan trọng hơn chỉ disable nút Import?

## 20. WorkManager từ enqueue đến kết quả

Đọc theo thứ tự:

1. TaskFlowApp và Manifest.
2. AppModule.provideWorkManager.
3. TaskViewModel.importSample.
4. [RefreshWorker.kt](app/src/main/java/com/example/taskflowtraining/data/worker/RefreshWorker.kt).
5. importWorkInfo/importResultToShow.
6. ListFragment.observeState và renderImportState.

### 20.1. WorkRequest

OneTimeWorkRequestBuilder<RefreshWorker> tạo yêu cầu một lần, không tạo periodic job.

Constraint NetworkType.CONNECTED yêu cầu mạng kết nối. Nó không đảm bảo DNS/server/API đang hoạt động.

Backoff EXPONENTIAL với khởi đầu 30 giây áp dụng khi retry. Đây là thời gian tối thiểu/định hướng lập lịch, không phải hẹn giờ chính xác 30 giây.

### 20.2. Unique work

Tên work: taskflow_sample_import.

ExistingWorkPolicy.KEEP giữ work chưa kết thúc cùng tên; request mới có thể không được dùng. Vì vậy source hỏi lại WorkManager để chọn activeWork thay vì luôn observe UUID vừa tạo.

KEEP không chặn enqueue mới khi chuỗi trước đã kết thúc. Không thay thế chống trùng ở database.

### 20.3. Enqueue xong khác import xong

operation.result.get() chờ thao tác enqueue hoàn tất. Nó không chờ Retrofit và các insert hoàn tất.

Kết quả thực thi lấy từ WorkInfo, còn danh sách Task lấy từ Room Flow.

### 20.4. Hilt assisted injection

WorkManager cung cấp appContext và WorkerParameters. Hilt cung cấp TaskRepository.

RefreshWorker không nhận Fragment, Activity, binding hoặc ViewModel. Worker có thể chạy khi màn hình danh sách không còn.

### 20.5. Result và retry

| Tình huống trong RefreshWorker | Kết quả |
|---|---|
| importSample hoàn tất | success |
| CancellationException | Ném lại |
| IOException, runAttemptCount < 2 | retry |
| IOException, hết giới hạn | failure |
| HTTP 429 hoặc 5xx, còn giới hạn | retry |
| HTTP khác hoặc hết giới hạn | failure |
| Exception khác | failure |

runAttemptCount bắt đầu từ 0. Nhánh retry khi <2 cho tối đa ba lần chạy theo chuỗi lỗi retry thông thường: attempt 0, 1, 2. Các tình huống bị hệ thống stop/reschedule cần phân biệt với cam kết mọi lần gọi HTTP đều đúng ba lần.

IOException có thể đến từ I/O khác ngoài mạng. Source hiện phân loại theo kiểu exception, không có taxonomy lỗi lưu trữ chi tiết.

### 20.6. Quan sát WorkInfo

importWorkInfo combine work list theo unique name với selectedImportId:

- Có selected ID → lấy đúng work.
- Chưa có → lấy work chưa finished.
- Không tìm thấy → null.

importResultToShow chỉ trả work finished chưa trùng acknowledgedImportId. UI Toast rồi acknowledge để giảm lặp trong cùng ViewModel.

selected/acknowledged ID chưa được persist. Process recreation không đảm bảo hiển thị đúng một lần kết quả cũ; khi không có selected ID, source chỉ tìm active work, nên kết quả vừa hoàn tất lúc UI vắng có thể không được chọn lại.

### 20.7. Không đồng nhất loading

TaskUiState.isLoading là busy của runAction CRUD/sort, không phải trạng thái running của Worker. btnImport kết hợp cả loading CRUD và work active.

**Bài tập:** mô tả ba trạng thái ENQUEUED vì chưa có mạng, RUNNING, ENQUEUED vì chờ retry; vì sao cùng ENQUEUED không đủ biết nguyên nhân?

## 21. Notification và Photo Picker

### 21.1. Notification là gửi ngay

Source hiện không đặt lịch theo thời điểm. Nhấn nhắc việc gửi notification ngay khi đủ điều kiện quyền.

Đừng suy ra đã có reminder Worker/AlarmManager chỉ từ tên “nhắc công việc”.

### 21.2. Ba phần phối hợp

- TaskFlowApp tạo channel task_reminders_high.
- DetailFragment xin POST_NOTIFICATIONS khi cần.
- [TaskNotificationHelper.kt](app/src/main/java/com/example/taskflowtraining/notification/TaskNotificationHelper.kt) dựng notification và PendingIntent.

Channel ID phải khớp ở Application và helper. ID là kỹ thuật, không dịch. Tên channel và title notification lấy resource.

IMPORTANCE_HIGH và PRIORITY_HIGH không đảm bảo mọi thiết bị luôn hiện heads-up: còn phụ thuộc cài đặt người dùng/hệ thống.

### 21.3. PendingIntent/deep link

NavDeepLinkBuilder cấu hình graph, destination Detail, taskId và MainActivity. Khi người dùng nhấn notification, hệ thống dùng PendingIntent để mở luồng tương ứng.

Nó khác callback lambda đang nằm trong bộ nhớ Fragment.

notify dùng tag task_reminder_<id> và notification id=0. Cặp tag/id phân biệt các Task; gửi lại cùng cặp sẽ cập nhật notification tương ứng.

### 21.4. Quyền

DetailFragment giữ pending Task ID/title để gửi sau callback cấp quyền. Hai field này chưa được bảo toàn chủ động qua process recreation.

Helper trả false nếu thiếu permission trên Android 13+. true ở đây có nghĩa code đã đi tới notify, không chứng minh người dùng chắc chắn nhìn thấy notification nếu channel/app notifications bị tắt.

### 21.5. Photo Picker

Activity Result API trả Uri:

~~~text
btnChoosePhoto → PickVisualMedia(ImageOnly)
→ callback Uri? → setImageURI → hiển thị ImageView
~~~

Uri là địa chỉ tài nguyên qua ContentResolver, không phải filesystem path để ghép chuỗi và mở File tùy ý.

checkImageUri chỉ mở stream và ghi log rồi đóng qua use. Nó chưa lưu ảnh vào Room, chưa copy ảnh, chưa lưu Uri gắn với taskId.

Vì thế ảnh hiện là hiển thị tạm trên View; không có chức năng ảnh persistence hoàn chỉnh. Callback cũng truy cập binding nên cần nghĩ tới lifecycle nếu sau này mở rộng.

**Bài tập:** chỉ ra nơi nào trong database đang lưu Uri. Đáp án hiện tại: không có.

## 22. Localization và UiText

Đọc:

- [values/strings.xml](app/src/main/res/values/strings.xml): English mặc định.
- [values-vi/strings.xml](app/src/main/res/values-vi/strings.xml): Vietnamese.
- [arrays.xml](app/src/main/res/values/arrays.xml).
- [UiText.kt](app/src/main/java/com/example/taskflowtraining/ui/UiText.kt).

### 22.1. Resource resolution

XML dùng @string/...; Fragment dùng getString; helper có Context dùng context.getString.

Hệ thống chọn tài nguyên theo locale/configuration. App không tự lưu enum EN/VI hoặc thay Context thủ công.

Task.title là nội dung do người dùng/API cung cấp, không tự được dịch khi đổi ngôn ngữ.

### 22.2. Vì sao UiText?

ViewModel lưu resourceId và args. Fragment resolve bằng Context hiện tại.

~~~text
ViewModel: UiText(error_http, [HTTP code])
→ Fragment: resolve(requireContext())
→ getString với %1$d
~~~

Không cần nhét Activity Context vào ViewModel để tạo một chuỗi.

UiText vẫn phụ thuộc resource ID của Android về mặt mô hình trình bày; đây là lựa chọn UI layer hiện tại, không phải domain model thuần.

### 22.3. Mapping lỗi

Throwable.toUiText ánh xạ một số message validation hiện có sang resource và dùng fallback cho lỗi khác.

Đây là cách tối thiểu giữ domain/model không cần Context, nhưng phụ thuộc nguyên văn message. Đổi message ở Repository mà không cập nhật mapping sẽ rơi vào fallback. Typed error là hướng có thể cân nhắc sau, không có sẵn trong source.

Thông báo kỹ thuật/debug chưa dịch không có nghĩa chúng đều được hiển thị nguyên văn cho người dùng.

### 22.4. Placeholder

%1$s: tham số chuỗi thứ nhất; %1$d: tham số số nguyên thứ nhất.

Resource EN/VI phải khớp kiểu và số tham số. Không ghép nhãn đã dịch bằng phép cộng nếu resource format đã cung cấp vị trí.

**Bài tập:** tìm resource priority_label rồi tìm caller truyền priorityText; giải thích vì sao title Task vẫn giữ tiếng Việt trong giao diện English.

## 23. Compose thực hành

File: [TaskCounter.kt](app/src/main/java/com/example/taskflowtraining/composepractice/TaskCounter.kt).

Đây là phần thực hành tách khỏi navigation XML; MainActivity chưa chuyển sang setContent và graph vẫn là các Fragment.

### 23.1. State hoisting

TaskCounter nhận count và onIncrement. Nó không sở hữu state counter.

TaskCounterScreen sở hữu count qua remember + mutableIntStateOf(0), truyền giá trị xuống và callback lên.

~~~text
TaskCounterScreen: count
   → TaskCounter(count)
   → Button click → onIncrement
   → count++
   → Compose lên lịch cập nhật composition
~~~

Đây là ví dụ dữ liệu đi xuống, hành động đi lên. Cùng tư duy với Adapter callbacks, nhưng cơ chế render khác XML Views.

### 23.2. remember không phải lưu database

remember giữ state qua recomposition tại vị trí composition tương ứng. Không đảm bảo giữ qua Activity recreation/process death.

Source chưa dùng rememberSaveable hoặc ViewModel cho counter. Đừng suy ra counter sống lâu như Room chỉ vì chạy Preview nhiều lần vẫn thấy số.

### 23.3. Recomposition

Composable có thể được gọi lại khi state đọc bởi nó thay đổi; cũng có thể được bỏ qua tùy cơ chế Compose. Log CHA/CON là công cụ quan sát trong bài tập, không phải cam kết số lần gọi cố định.

Không đặt side effect nghiệp vụ như insert Room trong body Composable chỉ vì “nó là hàm hiển thị”.

### 23.4. Layout hiện tại

TaskCounter gọi Text và Button nhưng chưa bọc Column/Row; import Row hiện không được sử dụng.

Gọi hai Composable liên tiếp không tự có nghĩa layout dọc như LinearLayout vertical. Cần hiểu parent layout trước khi giải thích vị trí/overlap. Đây là điểm để bạn tự học tiếp, không phải tài liệu tự triển khai bài Counter.

@Preview cung cấp điểm vào xem thử trong IDE; không tự thêm destination vào app.

**Tự kiểm tra:** nơi nào thực sự thay đổi count? Vì sao TaskCounter có thể tái sử dụng với state do ViewModel sở hữu về sau?

## 24. Gradle, compiler và dependency

### 24.1. Thứ tự đọc cấu hình

1. [settings.gradle.kts](settings.gradle.kts): repositories, tên project, include :app.
2. [build.gradle.kts](build.gradle.kts): plugin versions dùng chung.
3. [libs.versions.toml](gradle/libs.versions.toml): version catalog.
4. [app/build.gradle.kts](app/build.gradle.kts): Android config, dependencies, KSP.
5. [gradle.properties](gradle.properties): Gradle runtime options.
6. [wrapper properties](gradle/wrapper/gradle-wrapper.properties): distribution Gradle.
7. [daemon JVM](gradle/gradle-daemon-jvm.properties): JVM criteria cho daemon.

### 24.2. Phiên bản hiện tại đã đối chiếu trong phiên

| Thành phần | Phiên bản |
|---|---|
| AGP | 9.5.1 |
| Gradle Wrapper | 9.6.0 |
| Kotlin compiler app | 2.2.10, từng xác nhận kotlinCompilerClasspath |
| Compose Compiler Plugin | 2.2.10 |
| Compose BOM | 2025.12.00 |
| KSP | 2.3.10 |
| Hilt | 2.59.2 |
| Room | 2.8.4 |
| Coroutines | 1.10.2 |
| Serialization runtime sau sửa | 1.8.1 |
| minSdk / targetSdk / compileSdk | 26 / 37 / 37 |
| Java source/target | 17 |

Gradle daemon trong lần kiểm chứng trước chạy JDK 25 đi kèm Android Studio. Java target 17 không có nghĩa daemon nhất thiết phải chạy bằng JDK 17.

Kotlin hiển thị trong gradlew --version là Kotlin embedded cho Gradle, không tự chứng minh compiler ứng dụng có cùng version.

### 24.3. Built-in Kotlin và Compose

AGP 9 bật built-in Kotlin. Source không áp dụng org.jetbrains.kotlin.android.

Compose Compiler Plugin vẫn cần riêng và khớp Kotlin compiler. Không dùng placeholder KOTLIN_VERSION.

Compose BOM quản lý phiên bản bộ thư viện Compose, không quản lý version Compose compiler, Activity hoặc Lifecycle. Vì vậy activity-compose và lifecycle-runtime-compose vẫn có version riêng.

viewBinding=true và compose=true có thể cùng tồn tại. Bật Compose không tự migrate Fragment hoặc XML.

### 24.4. Dependency configurations

| Cấu hình | Dùng cho |
|---|---|
| implementation | Code production và dependency liên quan |
| debugImplementation | Chỉ debug, ví dụ ui-tooling |
| testImplementation | Local unit test |
| androidTestImplementation | Instrumented test |
| ksp | Annotation/symbol processor production |
| kspAndroidTest | Processor cho androidTest |

Room runtime là thư viện chạy. Room compiler là processor tạo implementation. Chỉ thêm runtime không thay thế compiler.

Hilt Android compiler và AndroidX Hilt compiler là hai artifact khác nhau; Hilt Worker dùng phần tích hợp AndroidX.

### 24.5. Catalog không phải mọi version thực tế

App đang khai báo nhiều dependency bằng literal trực tiếp, dù catalog còn có version cũ của cùng thư viện.

Nếu app không dùng alias thì chỉ đọc version catalog có thể kết luận sai. Muốn biết artifact được chọn, dùng dependencyInsight đúng configuration.

### 24.6. Build không phải mọi loại test

assembleDebug tạo APK debug và compile production. Nó không nhất thiết compile local unit tests hoặc chạy migration.

connectedDebugAndroidTest build test APK và chạy trên thiết bị. testDebugUnitTest chạy JVM tests sau khi compile test source set.

Không dùng “build PASS” để thay cho “migration PASS”.

## 25. Đọc test

### 25.1. Hai source set

| Vị trí | Môi trường | Ví dụ trong dự án |
|---|---|---|
| app/src/test | JVM local | validation, filter/sort, fake repository, ViewModel |
| app/src/androidTest | Android device/emulator | DAO, Hilt injection, migration |

Instrumented test cần runner và Android framework. Không phải vì test file viết Kotlin thì nó chạy trên JVM local giống nhau.

### 25.2. Arrange – Act – Assert

Arrange: tạo input/database/fake.

Act: gọi đúng hành vi muốn kiểm tra.

Assert: kiểm tra output/state/database.

Tên test là ý định, implementation assertion mới là bằng chứng thực tế.

### 25.3. Pure model tests

[TaskLogicTest.kt](app/src/test/java/com/example/taskflowtraining/model/TaskLogicTest.kt) và [TaskFilterAndSortTest.kt](app/src/test/java/com/example/taskflowtraining/model/TaskFilterAndSortTest.kt).

Đọc input, tự dự đoán kết quả, rồi mới xem assert. Bao gồm blank/title length, filtering, query trim, case-insensitive search, sort và tie-break ID.

Những test này không chứng minh DAO SQL, Hilt graph hoặc navigation đúng.

### 25.4. Fake Repository

[FakeTaskRepository.kt](app/src/test/java/com/example/taskflowtraining/model/FakeTaskRepository.kt) dùng MutableStateFlow, delay và failNextRefresh để điều khiển test.

Fake giúp tránh network/database thật và kiểm soát thời gian. Nhưng fake phải đủ gần contract production.

Hạn chế hiện tại:

- Thiếu observeTaskById của interface.
- Validation title tối đa 80, production tối đa 100 và trim.
- Import fake thêm mẫu mới mỗi lần, không mô phỏng remoteId deduplication.
- Update ID thiếu có hành vi khác production.

Không dùng fake để kết luận import thật không trùng.

### 25.5. Coroutine unit tests

MainDispatcherRule thay Dispatchers.Main bằng TestDispatcher và reset sau test.

runTest cung cấp scheduler thời gian ảo.

advanceTimeBy tiến thời gian, runCurrent chạy tác vụ đã sẵn sàng, advanceUntilIdle chạy công việc scheduler đến idle. Không phải ngủ thật 5 giây.

StateFlow dùng WhileSubscribed cần collector để upstream hoạt động. Test loading/error có backgroundScope collector cho mục đích đó.

### 25.6. Test ViewModel đang lệch production

[TaskViewModelTest.kt](app/src/test/java/com/example/taskflowtraining/TaskViewModelTest.kt) vẫn tạo ViewModel với repo/settings nhưng constructor production cần workManager.

Test ioException_updatesErrorState còn giả định vm.importSample gọi fake repository trực tiếp, trong khi production đã enqueue work. Chỉ thêm một mock WorkManager không tự làm kỳ vọng cũ trở nên đúng.

Lần chạy unit test trong phiên trước bị chặn compile do thiếu workManager và FakeTaskRepository thiếu method. Lượt tạo tài liệu không sửa hoặc chạy lại tests này.

### 25.7. DAO test

[TaskDaoTest.kt](app/src/androidTest/java/com/example/taskflowtraining/data/local/TaskDaoTest.kt):

- setUp tạo Room in-memory database riêng.
- tearDown đóng database.
- insert + findById kiểm tra lưu.
- observeAll().first() nhận emission đầu.
- update kiểm tra title thay đổi.
- delete kiểm tra findById trả null.

Tên updateTask_shouldChangeIsDone hiện không khớp nội dung: code thực tế đổi title thành "Update Task", không đổi isDone. Đây là ví dụ phải đọc assertion thay vì tin tên test.

In-memory test không kiểm tra migration từ file database cũ.

### 25.8. Hilt test

HiltTestRunner thay Application bằng HiltTestApplication cho test process. Nó không phải TaskFlowApp production có notification channel/factory setup giống hệt.

HiltInjectionTest dùng HiltAndroidRule rồi inject hai InstanceTracker và assertSame. Nó chứng minh scope cho binding đó trong test, không chứng minh mọi dependency graph/case Worker.

File data/test.kt chỉ là class trống, không có @Test nên không phải kiểm thử hành vi.

**Bài tập:** với mỗi test, viết một câu “test này chứng minh ... nhưng không chứng minh ...”.

## 26. Migration 1→2 và schema JSON

Đọc đúng thứ tự:

1. [1.json](app/schemas/com.example.taskflowtraining.data.local.TaskDatabase/1.json)
2. TaskEntity hiện tại và [2.json](app/schemas/com.example.taskflowtraining.data.local.TaskDatabase/2.json)
3. [TaskMigrations.kt](app/src/main/java/com/example/taskflowtraining/data/local/TaskMigrations.kt)
4. AppModule.provideDatabase.
5. [TaskMigrationTest.kt](app/src/androidTest/java/com/example/taskflowtraining/data/TaskMigrationTest.kt).

### 26.1. Phân biệt ba thứ

| Thành phần | Nội dung |
|---|---|
| Entity hiện tại | Cấu trúc ứng dụng hiện mong muốn |
| Schema JSON | Bản mô tả version cụ thể đã export, phục vụ lịch sử/test |
| Migration | Lệnh chuyển database đang tồn tại từ version cũ sang mới |

JSON không phải dữ liệu Task người dùng. Không có danh sách các Task thực tế trong 1.json/2.json.

Không chỉnh tay 1.json để nó giống v2; làm vậy sẽ phá ý nghĩa đầu vào lịch sử.

### 26.2. Đọc từng phần JSON

formatVersion: version định dạng file export, không phải database version.

database.version: 1 hoặc 2.

identityHash: dấu nhận dạng schema được Room sử dụng, không phải hash dữ liệu người dùng hoặc mật khẩu.

entities: mô tả các bảng.

createSql: câu lệnh tạo cấu trúc của version đó. Placeholder TABLE_NAME là chi tiết file schema export, không phải key cần dịch.

fields: columnName, affinity, notNull, defaultValue.

primaryKey: autoGenerate và columnNames.

indices: index_tasks_remoteId, unique=true, remoteId.

setupQueries: metadata room_master_table và identity hash.

### 26.3. Khác biệt v1 → v2

V1 có id/title/isDone/priority/remoteId.

V2 thêm description TEXT NOT NULL DEFAULT ''.

Index remoteId vẫn giữ nguyên. Không có version 3 trong source đã đọc.

### 26.4. Migration SQL

~~~sql
ALTER TABLE tasks
ADD COLUMN description TEXT NOT NULL DEFAULT ''
~~~

ADD COLUMN thay đổi cấu trúc. DEFAULT '' cung cấp giá trị cho hàng cũ, đáp ứng NOT NULL.

Không cần xóa bảng hoặc copy dữ liệu sang database mới cho thay đổi này.

@Database(version=2) chỉ cho Room biết version mong muốn; tự tăng số version không tự sinh migration SQL này.

### 26.5. Đăng ký production

AppModule thêm addMigrations(MIGRATION_1_2). Migration tồn tại trong file nhưng không đăng ký thì production builder không tự biết sử dụng.

Khi mở database đang ở v1, Room chọn đường migration đến v2 và kiểm tra cấu trúc kết quả. Database mới tạo trực tiếp ở v2 không phải bài kiểm tra đường nâng cấp.

### 26.6. Export và assets

ksp room.schemaLocation trỏ tới app/schemas. exportSchema=true cho phép processor xuất mô tả.

androidTest assets.srcDir("$projectDir/schemas") đưa schema vào assets của test APK để MigrationTestHelper đọc được.

Có file trên máy phát triển chưa đủ: nếu không đóng gói vào test assets thì createDatabase vẫn có thể không tìm thấy schema.

### 26.7. Đọc test theo dòng thời gian

1. Helper được cấu hình với TaskDatabase class.
2. createDatabase("migration-test", 1) dựng cấu trúc v1 từ schema.
3. Insert hàng id=1, title="Học Room Migration", isDone=0, priority=2, remoteId=NULL.
4. Đóng kết nối v1.
5. runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2).
6. Query hàng id=1 sau migration.
7. Assert có hàng, title giữ nguyên, priority=2, isDone=0, description="".
8. finally đóng migrated database.

Boolean true ở validate là kiểm tra các bảng bị drop theo chính sách validateDroppedTables, không phải cờ “bảo toàn dữ liệu tự động”.

### 26.8. Schema validation khác data assertion

runMigrationsAndValidate kiểm tra cấu trúc schema mong đợi.

assertTrue(cursor.moveToFirst()) + assertEquals kiểm tra hàng cụ thể và giá trị dữ liệu. Migration làm đúng cấu trúc nhưng xóa hết dữ liệu vẫn có thể cần các assert này để bị phát hiện.

Test hiện có một hàng với isDone=false. PASS không chứng minh mọi giá trị, nhiều hàng, chuỗi Unicode dài, remoteId non-null hoặc đường upgrade tương lai đều đúng.

### 26.9. Bằng chứng đã có

Trong lượt sửa Serialization trước, connectedDebugAndroidTest chạy riêng migrate1To2_preservesData trên emulator-5554 API 37. Report có tests=1, failures=0, errors=0, skipped=0. Đây là test PASS thực tế của snapshot đó.

Không chạy lại test trong lượt tạo tài liệu. Lịch sử PASS không thay thế việc test sau các thay đổi source tương lai.

**Bài tập:** thử giải thích vì sao Kotlin default "" không đủ thay thế DEFAULT '' trong ALTER TABLE, nhưng không tự sửa schema để thử trên dữ liệu thật.

## 27. Lỗi Serialization đã gặp

### 27.1. Triệu chứng

AbstractMethodError ở GeneratedSerializer.typeParametersSerializers khi MigrationTestHelper.createDatabase đọc schema.

Điểm lỗi xảy ra trước khi SQL migration thực thi. Vì vậy không có căn cứ sửa ALTER TABLE hoặc xóa database để chữa lỗi này.

AbstractMethodError gợi ý bytecode/library runtime không khớp API/ABI mà code đã được biên dịch dựa vào.

### 27.2. Dependency graph thực tế trước sửa

~~~text
DataStore / SavedState / Lifecycle
  → Serialization runtime app 1.7.3

Room migration 2.8.4
  → yêu cầu Serialization JSON 1.8.1

AGP consistent resolution:
debugAndroidTestRuntimeClasspath theo debugRuntimeClasspath
  → test bị ràng buộc dùng 1.7.3
  → schema deserializer không chạy tương thích
~~~

Không tìm thấy khóa 1.7.3 trực tiếp trong build scripts, lockfile hay custom resolution strategy của repository. “strictly 1.7.3” trong report đến từ consistent resolution, không nhất thiết do bạn viết strictly trong Gradle.

### 27.3. Sửa đã áp dụng

app/build.gradle.kts có constraints implementation cho core và json 1.8.1 với because giải thích yêu cầu Room.

Đặt ở app implementation để app runtime resolve lên phiên bản phù hợp, rồi test đi theo. Chỉ pin androidTest có thể không giải quyết việc app runtime vẫn áp constraint cũ.

Không dùng force/exclude. Constraint không tự thêm artifact nếu dependency đó chưa có trong graph.

### 27.4. Kết quả đã kiểm chứng

dependencyInsight cho core/core-jvm và json/json-jvm chọn 1.8.1. BOM cũng là 1.8.1. Build debug và migration test PASS.

Thay đổi runtime app có thể ảnh hưởng các consumer khác dùng Serialization. Chưa chạy toàn bộ regression suite nên không kết luận mọi test đều không ảnh hưởng.

### 27.5. Cách đọc dependencyInsight

- “1.8.1 → 1.7.3”: requested khác selected.
- “By constraint”: có ràng buộc tác động lựa chọn.
- “version resolved in ... by consistent resolution”: xem configuration nguồn.
- Đọc cả artifact gốc và biến thể -jvm.
- Đọc đường dẫn từ consumer tới artifact, không chỉ một dòng BOM.

**Bài tập:** vì sao “nâng Room lên mới nhất” hoặc “force JSON trong test” chưa phải kết luận đúng nếu chưa xem runtime app?

## 28. State sống ở đâu và trong bao lâu

| State/dữ liệu | Owner/lưu trữ hiện tại | Qua recomposition/View render | Qua rotation | Qua process recreation |
|---|---|---|---|---|
| Hàng Task | Room file | Có | Có | Có, nếu dữ liệu/file còn |
| Sort | DataStore | Có | Có | Có, đọc lại từ disk |
| Query/filter/priority filter | TaskViewModel memory | Có | Thường giữ khi ViewModel được retain | Chưa lưu chủ động |
| taskId Detail/Edit | Nav args + SavedStateHandle | Có | Có theo cơ chế owner | Có thể được framework restore theo saved state hợp lệ |
| Nội dung chưa lưu Edit | EditText, cơ chế render hiện tại | Có trong View hiện tại | Chưa đảm bảo mọi tương tác restore | Chưa có quản lý draft |
| Save Success | EditViewModel StateFlow | Replay giá trị gần nhất | Có thể replay | Không persist riêng |
| WorkRequest | WorkManager persistent storage | Không phụ thuộc View | Không phụ thuộc View | Có thể được lập lịch/khôi phục theo hệ thống |
| selected/acknowledged work ID | TaskViewModel memory | Có | Thường giữ cùng VM | Không persist |
| Ảnh được chọn | ImageView | Trong View hiện tại | Chưa bảo toàn chủ động | Không persist |
| Counter | Compose remember | Có qua recomposition phù hợp | Không được bảo toàn bằng remember đơn thuần | Không |
| Pending notification task | Field của DetailFragment | Trong instance hiện tại | Chưa có lưu chủ động | Không |

Rotation không đồng nghĩa process death. Bấm Back, Home, force-stop và hệ thống reclaim process cũng không phải cùng một tình huống.

Không dùng force-stop như bằng chứng duy nhất để kiểm tra mọi trường hợp WorkManager/process recreation; force-stop có hành vi hệ thống riêng.

## 29. Những hạn chế cần nhận biết

Đây là ghi nhận phục vụ đọc code, không phải danh sách đã được tự sửa.

| Điểm | Bằng chứng/ý nghĩa | Phạm vi |
|---|---|---|
| description chưa có trong Task model | Entity v2 có, mapper/model chưa có | Bài migration chưa thành feature mô tả |
| Draft Edit chưa được quản lý chủ động | etTitle + displayedTaskId | Lifecycle/UI |
| Success là state replay | observeSaveState navigateUp | Navigation/event |
| Hai collector cùng dùng tvMessage Edit | render uiState và saveState | Race hiển thị |
| Search/filter chưa persist | MutableStateFlow khởi tạo default | Process recreation |
| Upstream list/DataStore lỗi chưa có catch riêng | combine ngoài runAction | Error resilience |
| Import không transaction toàn bộ | loop insert trong Repository | Partial success |
| read-copy-update có cửa sổ cạnh tranh | findById rồi update | Concurrent writes |
| Work result IDs chỉ nằm memory | selected/acknowledged fields | Khôi phục feedback |
| Ảnh chỉ hiển thị, chưa lưu | setImageURI, không có field URI | Photo feature |
| Notification gửi ngay | Không có scheduler reminder | Phạm vi reminder |
| Unit tests lệch constructor/interface | Thiếu WorkManager và observeTaskById | Test compile |
| DAO test tên không khớp assertion | ChangeIsDone nhưng đổi title | Test clarity |
| UiText map theo message | when(message) | Maintainability |
| Catalog và literal versions khác nhau | App khai báo trực tiếp nhiều artifact | Build understanding |

Không cần refactor toàn bộ sang Clean Architecture, chuyển Compose hoặc đổi package để giải quyết mỗi mục. Hãy chọn theo bài học và bằng chứng lỗi thực tế.

## 30. Tra cứu theo triệu chứng

| Triệu chứng | Đọc đầu tiên | Cách suy luận |
|---|---|---|
| Mở app không ra List | Manifest, MainActivity, activity_main, nav_graph | Chuỗi entry point → host → startDestination |
| Detail trắng | DetailFragment constructor/onCreateView | Có View được inflate không? onViewCreated có chạy không? |
| Nhấn title không chuyển màn | Adapter callback, openTaskDetail, action ID | Đi theo từng callback và Bundle |
| Detail NotFound | taskId, DAO observeById, local/remote ID | Có truyền nhầm remoteId không? |
| Add không hiện hàng | runAction error → Repository validation → DAO → Flow/filter | Hàng có thể bị filter che |
| Checkbox tự kích hoạt khi scroll | onBindViewHolder listener | Gán isChecked có gọi callback cũ không? |
| Priority Spinner sai nhưng list đã lọc | ListState → TaskUiState → render | Có quên copy priorityFilter vào state? |
| Back mất filter | VM owner, callbacks restore, state persistence | Có tạo VM/entry mới hoặc callback ghi default không? |
| Edit tự mất chữ đang gõ | displayedTaskId, View recreation, render | Data state và draft đang ở đâu? |
| Nhấn Save quay lui nhiều lần | Success replay, collector lifecycle | State có được consume/reset không? |
| Import chỉ chờ | WorkInfo, constraint, backoff | Enqueued vì mạng hay lịch? |
| Import bị trùng | remoteId mapping, unique index, IGNORE | Trùng ID hay chỉ trùng title? |
| API lỗi nhưng không có HTTP message | Worker catch | Worker hiện trả failure không kèm chi tiết |
| Notification không thấy | permission, channel ID/settings, notify | Boolean helper không chứng minh UI hệ thống hiển thị |
| Ảnh mất sau quay lại | Callback và nơi persist | Source chưa lưu URI |
| Schema JSON không xuất | exportSchema, room.schemaLocation, KSP task | Build có chạy processor không? |
| Schema test không tìm thấy JSON | androidTest assets, database FQN | File có được đóng gói không? |
| Migration schema mismatch | SQL vs Entity/default vs 2.json | Phân biệt nullability/default/type |
| AbstractMethodError Serialization | runtime dependencyInsight | Tìm consistent resolution từ app |
| Build PASS nhưng unit tests lỗi | src/test và task được chạy | assembleDebug chưa chạy unit tests |

Nguyên tắc debug: giữ nguyên dữ liệu; đọc lỗi gốc đầu tiên; kiểm tra một giả thuyết tại một thời điểm. Không clean, xóa database, đổi hàng loạt version hoặc sửa migration SQL khi stacktrace không chỉ tới chúng.

## 31. Kế hoạch tự đọc và bài tập

Đây là lịch đọc đề xuất theo buổi, không thay giáo trình Ngày 22–27 và không tự động chuyển bạn sang bài mới.

### Buổi 1 — Entry point và màn đầu

Đọc Manifest → Application → Activity → activity_main → nav_graph.

Sản phẩm: một sơ đồ giải thích vì sao không có new ListFragment trong MainActivity mà danh sách vẫn hiện.

### Buổi 2 — Một Task từ input tới Room

Đọc setupInputs btnAdd → ViewModel.add/runAction → Repository.add → validateTitle → DAO.insert.

Sản phẩm: bảng input/output/error từng bước. Đánh dấu title được trim ở đâu, priority validate ở đâu, etTitle clear lúc nào.

### Buổi 3 — Room quay lại UI

Đọc observeAll → observeTasks → combine/listState/uiState → observeState → render → Adapter.

Sản phẩm: giải thích vì sao không gọi reload sau add.

### Buổi 4 — Filter/sort

Dự đoán kết quả của một danh sách 5 Task với query, completed và priority. Sau đó đối chiếu filterAndSort và test.

Sản phẩm: phân biệt sort label, enum name, ID order và dữ liệu lưu DataStore.

### Buổi 5 — RecyclerView và lifecycle

Đọc holder, binding, DiffUtil, checkbox listeners, onDestroyView và repeatOnLifecycle.

Sản phẩm: giải thích hai lỗi giả định: collector render View đã destroy; checkbox ghi lại database khi bind.

### Buổi 6 — Detail/Edit

Đi theo taskId từ Adapter đến SavedStateHandle, DAO nullable, Detail state; tiếp tục Edit save.

Sản phẩm: state machine của Detail và hai state machine load/save của Edit. Ghi phần draft còn thiếu.

### Buổi 7 — Hilt

Đọc constructor từng lớp rồi tìm provider tương ứng.

Sản phẩm: graph không bỏ sót Context, Database, API, Repository, Settings và WorkManager.

### Buổi 8 — Import remote

Đọc API, DTO mapper, take(20), unique index và insert count.

Sản phẩm: diễn giải import hai lần, import bị hủy giữa chừng và xóa Task mẫu rồi import lại.

### Buổi 9 — WorkManager

Đọc request → KEEP → selected work → Worker errors → WorkInfo → acknowledge.

Sản phẩm: phân biệt enqueue thành công, work thành công và số bản ghi mới.

### Buổi 10 — Notification/Photo/EN-VI

Sản phẩm: chỉ rõ dữ liệu nào được persist, dữ liệu nào tạm, ID nào tuyệt đối không dịch, UI text nào resolve theo locale.

### Buổi 11 — Compose riêng

Đọc Counter và giải thích owner count, callback, remember, recomposition và layout parent. Chưa viết full feature thay XML.

### Buổi 12 — DAO test và migration

Đọc v1 JSON → migration → v2 JSON → test. Tự giải thích từng assertion và giới hạn test một hàng.

### Buổi 13 — Toolchain và lỗi dependency

Đọc configs và constraint Serialization.

Sản phẩm: giải thích một người thấy Kotlin 2.3.21 trong Gradle version nhưng compiler app 2.2.10 không phải mâu thuẫn.

### Buổi 14 — Tự kể lại dự án

Không mở code, thử trình bày 10 phút:

1. App mở List thế nào?
2. Add đi đâu và kết quả về bằng gì?
3. Filter/sort sống ở đâu?
4. Detail lấy Task thế nào?
5. Edit lưu rồi Detail cập nhật thế nào?
6. Hilt tạo dependency gì?
7. Import sống ngoài màn hình thế nào?
8. Notification mở đúng Task ra sao?
9. Migration giữ dữ liệu thế nào?
10. Test nào chứng minh điều gì?

Chỗ nào kể chưa rõ thì quay lại chương đó; không cần đọc lại toàn bộ từ đầu.

## 32. Câu hỏi tổng kết và đáp án

Tự trả lời trước khi đọc dòng “Đáp án”.

### Câu 1. Fragment có gọi Retrofit trực tiếp không?

Đáp án: không trong luồng Task hiện tại. UI qua ViewModel; import hiện qua WorkManager/Worker rồi Repository/API.

### Câu 2. Vì sao add thành công mà không thấy Task?

Đáp án: có thể kết quả bị query/filter/priority che. Kiểm tra Room và filter state trước khi kết luận insert hỏng.

### Câu 3. Task và Entity có luôn cùng field không?

Đáp án: không. Entity v2 có description, model Task chưa có.

### Câu 4. Data class copy có tự ghi Room?

Đáp án: không; dao.update mới thực hiện persistence.

### Câu 5. suspend có nghĩa chạy trên IO?

Đáp án: không. Phải xét API và dispatcher; source có Future.get được chuyển sang IO rõ ràng.

### Câu 6. WhileSubscribed(5000) có polling database mỗi 5 giây không?

Đáp án: không; đó là timeout trước khi dừng upstream sau khi mất subscriber.

### Câu 7. StateFlow có tự xử lý Toast/navigation đúng một lần?

Đáp án: không. Nó replay state gần nhất. Source có acknowledge cho import trong VM memory, còn Edit Success chưa có consume.

### Câu 8. Sorted theo NEWEST dựa vào createdAt?

Đáp án: không, theo id giảm dần.

### Câu 9. ID remote có dùng trực tiếp làm taskId navigation?

Đáp án: không nên; DAO truy local primary key. remoteId là cột khác.

### Câu 10. Import retry chắc chắn tạo trùng?

Đáp án: không với cùng remoteId đang tồn tại vì unique + IGNORE, nhưng title trùng, remote ID khác hoặc hàng đã xóa là chuyện khác.

### Câu 11. KEEP có ngăn import lại ngày mai?

Đáp án: không; nó giữ work chưa finished cùng tên.

### Câu 12. Nhấn reminder có lên lịch 30 phút sau?

Đáp án: không; code gửi notification ngay sau kiểm tra/xin quyền.

### Câu 13. Chọn ảnh có lưu vào Room?

Đáp án: chưa. Chỉ set ImageView và đọc URI thử.

### Câu 14. Tăng version=2 có tự thêm description vào database cũ?

Đáp án: không; cần migration được đăng ký và schema kết quả khớp.

### Câu 15. 1.json có chứa Task người dùng?

Đáp án: không; nó chứa mô tả schema và metadata.

### Câu 16. MigrationTestHelper chỉ cần v2 Entity?

Đáp án: không cho test đường v1→v2 này; helper cần schema lịch sử được đóng gói assets để tạo database cũ và validate.

### Câu 17. Migration test PASS có chứng minh production builder đã đăng ký migration?

Đáp án: test truyền MIGRATION_1_2 trực tiếp cho helper; muốn kết luận registration production phải đọc AppModule hoặc test builder production. Source AppModule hiện đã addMigrations.

### Câu 18. Build debug PASS có nghĩa unit tests PASS?

Đáp án: không; các source set/task khác nhau. Source unit test hiện còn lệch contract.

### Câu 19. Room lỗi Serialization thì nên sửa ALTER TABLE?

Đáp án: không khi lỗi đang ở schema deserializer trước bước SQL. Dependency graph là nơi cần kiểm tra.

### Câu 20. Có phải mọi thứ trong dự án đã theo kiến trúc mục tiêu?

Đáp án: chưa. Layer đang phân tách ở mức học tập; feature packages/domain packages chưa di chuyển đầy đủ. Điều đó không ngăn bạn hiểu và kiểm thử luồng hiện tại.

## 33. Từ điển nhanh và tài liệu tham khảo

### 33.1. Từ điển

| Từ | Cách hiểu trong TaskFlowV3 |
|---|---|
| UI State | Dữ liệu đủ để render trạng thái màn hình |
| Event/action | Ý định thao tác, ví dụ click Save |
| Repository | Ranh giới truy cập và phối hợp dữ liệu |
| DAO | Hợp đồng truy vấn Room |
| DTO | Cấu trúc response API |
| Entity | Cấu trúc persistence |
| Mapper | Chuyển biểu diễn dữ liệu |
| Lifecycle owner | Thành phần sở hữu vòng đời công việc/state |
| Collector | Nơi nhận giá trị từ Flow |
| Upstream | Nguồn và các operator phía trước collector |
| Back stack | Lịch sử destination được Navigation quản lý |
| Constraint | Điều kiện WorkManager hoặc ràng buộc dependency, tùy ngữ cảnh |
| Retry | Yêu cầu WorkManager chạy lại khi phù hợp lịch |
| Backoff | Chính sách khoảng chờ sau retry |
| Schema | Cấu trúc bảng/cột/index/default |
| Migration | Các bước chuyển schema version |
| ABI mismatch | Bytecode và runtime library không tương thích hợp đồng binary |
| KSP | Công cụ xử lý symbol để sinh mã |
| BOM | Tập version constraints phối hợp cho nhóm thư viện |
| Recomposition | Compose thực thi/cập nhật composition dựa trên state |

### 33.2. Tài liệu chính thức để đào sâu

Các link là tài liệu tham khảo. Khi triển khai API mới, kiểm tra nội dung và version hiện tại; đừng sao chép version của ví dụ vào dự án một cách máy móc.

- [Android app architecture](https://developer.android.com/topic/architecture)
- [Fragment lifecycle](https://developer.android.com/guide/fragments/lifecycle)
- [ViewBinding](https://developer.android.com/topic/libraries/view-binding)
- [Lifecycle và coroutines](https://developer.android.com/topic/libraries/architecture/coroutines)
- [StateFlow/SharedFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
- [Room](https://developer.android.com/training/data-storage/room)
- [Room migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [Hilt và Jetpack/WorkManager](https://developer.android.com/training/dependency-injection/hilt-jetpack)
- [WorkManager configuration](https://developer.android.com/develop/background-work/background-tasks/persistent/configuration/custom-configuration)
- [WorkRequest/constraint/backoff](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work)
- [Unique work và quan sát work](https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/manage-work)
- [Navigation](https://developer.android.com/guide/navigation)
- [Photo Picker](https://developer.android.com/training/data-storage/shared/photopicker)
- [Localization](https://developer.android.com/guide/topics/resources/localization)
- [Compose state](https://developer.android.com/develop/ui/compose/state)
- [Built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin)
- [Compose compiler migration](https://kotlinlang.org/docs/compose-compiler-migration-guide.html)
- [Gradle dependency constraints](https://docs.gradle.org/current/userguide/dependency_constraints.html)
- [JSONPlaceholder](https://jsonplaceholder.typicode.com/)

### 33.3. Bước học đầu tiên sau khi mở tài liệu

Chỉ làm một việc:

Mở MainActivity.kt, activity_main.xml và nav_graph.xml. Tự viết 5–7 câu giải thích vì sao MainActivity chỉ setContentView nhưng ListFragment xuất hiện.

Khi giải thích được câu đó, chuyển sang theo dõi một Task từ btnAdd tới Room rồi quay lại Adapter. Không cần học thuộc tất cả annotation trước.
