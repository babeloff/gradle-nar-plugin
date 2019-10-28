

enum class NifiManifestExtension(val manifestKey: String)
{
    Nifi_GROUP("Nifi-Group"),
    Nifi_ID("Nifi-Id"),
    Nifi_VERSION("Nifi-Version"),
    Nifi_DEPENDENCY_GROUP("Nifi-Dependency-Group"),
    Nifi_DEPENDENCY_ID("Nifi-Dependency-Id"),
    Nifi_DEPENDENCY_VERSION("Nifi-Dependency-Version")
}