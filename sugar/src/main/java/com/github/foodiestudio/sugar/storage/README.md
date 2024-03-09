## Storage
> 自从引入 Scoped Storage 之后，一般业务开发真的不需要申请相关权限。

仅讨论 Android 10 以上，不涉及额外权限的所有情况

- 对应应用专属文件目录下的操作，可以直接使用 File 的 API
- 对非媒体文件的读写操作：
    - 需要 SAF 的方式（唯一的方式），可以引导用户去选择你想要的路径（如：Document/）：最终是基于 uri
      来操作，中间的抽象类是官方维护的 DocumentFile，需要注意的是，并不是所有 SAF
      的「文件」都有文件路径，因为有些是虚拟文件。
  - 为了方便后续的处理，可以默认申请（对用户无感）对这个 uri 的长久访问，这样设备重启之后也能访问。（用户可以在应用详情里取消这个授权）
- 对媒体文件的读写操作，更标准一点，使用对应的 MediaStore API：
  - 对于媒体文件基本没有局部修改的需求，所以也就是加载、另存为和删除，以及少有的移动.
  - 也是基于 uri 进行操作，与 DocumentUri 格式不同，并且是可以查询的到对应的文件的全路径的。（因为都是在本地）

## API
### AppFileHelper

AppFileHelper 涵盖了对文件操作的几种常见操作，具体可以分为：
- fileSystem: 基于 okio 风格的 file path 操作文件，支持对 File path 以及 document uri
- 以及提供了创建文件夹等

### MediaFile

MediaFile 是对 MediaStore 相关的封装，提供了一些常见的需求。

```kotlin
// write big file
MediaFile.create(context = context, MediaStoreType.Audio, "hello.mp3", true).let {
    it.write {
        // ...write data 
    }
    it.releasePendingStatus()
}
```

## Known issue

1. okio 的 FileSystem 的部分 API 并不适用于 Android 的 uri，对应的一些场景需要额外处理：
    - createDirectory/createFile: 使用 TreeDocumentFile 的对应 API
2. 没有考虑查询的情况（查询仅针对 MediaFile，否则需要申请 Manage Storage 这个需要 Google Play 审核的权限）：
    - 一般的查询涉及访问别的应用创建的，所以需要申请对应的读取权限，


### Q&A

> Uri 可以作为 File path 的替代吗？

- uri 是稳定的，如果是通过 SAF 方式获取的 document 的 uri 是还可以申请长久的获取访问权限。
- 基于 uri 也可以直接完成对文件的读写需求，日常业务需求方面和 file path 并没有太大区别

> 如果在 Native 要访问 File，还可以继续依赖 File path 吗？

对于应用专属目录，是可以继续用的，但如果不是，还是传递 FileDescriptor 的指针来实现吧。

### Thanks
- https://github.com/google/modernstorage

