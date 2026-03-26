import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

enum class RuStoreArtifactType {
    APK,
    AAB
}

enum class RuStorePublishType {
    MANUAL,
    INSTANTLY,
    DELAYED
}

abstract class RuStorePublishingExtension @Inject constructor(
    objects: ObjectFactory
) {
    val packageName: Property<String> = objects.property(String::class.java)
    val artifactType: Property<RuStoreArtifactType> = objects.property(RuStoreArtifactType::class.java)
    val keyId: Property<String> = objects.property(String::class.java)
    val privateKey: Property<String> = objects.property(String::class.java)
    val appType: Property<String> = objects.property(String::class.java)
    val category: Property<String> = objects.property(String::class.java)
    val minAndroidVersion: Property<String> = objects.property(String::class.java)
    val developerEmail: Property<String> = objects.property(String::class.java)
    val developerWebsite: Property<String> = objects.property(String::class.java)
    val developerVkCommunity: Property<String> = objects.property(String::class.java)
    val publishType: Property<RuStorePublishType> = objects.property(RuStorePublishType::class.java)
    val publishDateTime: Property<String> = objects.property(String::class.java)
    val partialValue: Property<Int> = objects.property(Int::class.java)
    val releaseNotes: Property<String> = objects.property(String::class.java)
    val priorityUpdate: Property<Int> = objects.property(Int::class.java)
    val artifactFile: RegularFileProperty = objects.fileProperty()
}
