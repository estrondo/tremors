package toph.service

import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import toph.TimeService
import toph.TophSpec
import toph.model.objectstorage.FolderPath
import toph.model.objectstorage.ObjectContent
import toph.model.objectstorage.ObjectItem
import toph.model.objectstorage.ObjectPath
import toph.model.objectstorage.ObjectStorageContextFixture
import toph.repository.ObjectStorageRepository
import toph.service.ObjectStorageService.GetContext
import tremors.ZonedDateTimeFixture
import tremors.generator.KeyGenerator
import zio.Task
import zio.ZIO

trait ObjectStorageServiceSpec extends TophSpec:

  protected val nextTimeService =
    ZIO.serviceWith[TimeService] { x =>
      val now = ZonedDateTimeFixture.createRandom()
      Mockito.when(x.zonedDateTimeNow()).thenReturn(now)
      now
    }

  protected val nextKeyGenerator =
    ZIO.serviceWith[KeyGenerator] { x =>
      val key = KeyGenerator.short()
      Mockito.when(x.short()).thenReturn(key)
      Mockito.when(x.medium()).thenReturn(key)
      Mockito.when(x.long()).thenReturn(key)
      Mockito.when(x.generate(ArgumentMatchers.any())).thenReturn(key)
      key
    }

  protected val nextContext =
    ZIO.serviceWith[GetContext] { x =>
      val ctx = ObjectStorageContextFixture.createRandom()
      Mockito.when(x(ArgumentMatchers.any())).thenReturn(ctx)
      ctx
    }

  protected def repositoryLoadFolder(path: FolderPath, result: Task[Seq[ObjectItem]]) =
    ZIO.serviceWithZIO[ObjectStorageRepository] { x =>
      Mockito.when(x.load(path)).thenReturn(result)
      result
    }

  protected def repositoryLoadObject(path: ObjectPath, result: Task[Option[ObjectContent]]) =
    ZIO.serviceWith[ObjectStorageRepository] { x =>
      Mockito.when(x.load(path)).thenReturn(result)
      result
    }
