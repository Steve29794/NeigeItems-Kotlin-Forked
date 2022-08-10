package pers.neige.neigeitems.utils

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import taboolib.common.platform.function.getDataFolder
import java.io.File

object ConfigUtils {
    /**
     * 获取文件夹内所有文件
     * @param dir 待获取文件夹
     * @return 文件夹内所有文件
     */
    @JvmStatic
    fun getAllFiles(dir: File): ArrayList<File> {
        val list = ArrayList<File>()
        val files = dir.listFiles() ?: arrayOf<File>()
        for (file: File in files) {
            if (file.isDirectory) {
                list.addAll(getAllFiles(file))
            } else {
                list.add(file)
            }
        }
        return list
    }

    /**
     * 获取文件夹内所有文件
     * @param dir 待获取文件夹路径
     * @return 文件夹内所有文件
     */
    @JvmStatic
    fun getAllFiles(dir: String): ArrayList<File> {
        return getAllFiles(File(getDataFolder(), File.separator + dir))
    }

    /**
     * 获取文件夹内所有文件
     * @param plugin 待获取文件夹归属插件
     * @param dir 待获取文件夹路径
     * @return 文件夹内所有文件
     */
    @JvmStatic
    fun getAllFiles(plugin: Plugin, dir: String): ArrayList<File> {
        return getAllFiles(File(plugin.dataFolder, File.separator + dir))
    }

    /**
     * 克隆ConfigurationSection
     * @return 对应ConfigurationSection的克隆
     */
    @JvmStatic
    fun ConfigurationSection.clone(): ConfigurationSection {
        val tempConfigSection = YamlConfiguration() as ConfigurationSection
        this.getKeys(false).forEach { key ->
            tempConfigSection.set(key, this.get(key))
        }
        return tempConfigSection
    }

    /**
     * 获取文件中所有ConfigurationSection
     * @return 文件中所有ConfigurationSection
     */
    @JvmStatic
    fun File.getConfigSections(): ArrayList<ConfigurationSection> {
        val list = ArrayList<ConfigurationSection>()
        val config = YamlConfiguration.loadConfiguration(this)
        config.getKeys(false).forEach { key ->
            config.getConfigurationSection(key)?.let { list.add(it) }
        }
        return list
    }

    /**
     * 获取所有文件中所有ConfigurationSection
     * @return 文件中所有ConfigurationSection
     */
    @JvmStatic
    fun ArrayList<File>.getConfigSections(): ArrayList<ConfigurationSection> {
        val list = ArrayList<ConfigurationSection>()
        for (file: File in this) {
            list.addAll(file.getConfigSections())
        }
        return list
    }

    /**
     * 获取文件中所有顶级节点内容
     * @return 文件中所有顶级节点内容
     */
    @JvmStatic
    fun File.getContents(): ArrayList<Any> {
        val list = ArrayList<Any>()
        val config = YamlConfiguration.loadConfiguration(this)
        config.getKeys(false).forEach { key ->
            config.get(key)?.let { list.add(it) }
        }
        return list
    }

    /**
     * 获取文件中所有顶级节点内容
     * @return 文件中所有顶级节点内容
     */
    @JvmStatic
    fun ArrayList<File>.getContents(): ArrayList<Any> {
        val list = ArrayList<Any>()
        for (file: File in this) {
            list.addAll(file.getContents())
        }
        return list
    }

    /**
     * 用于 ConfigurationSection 转 HashMap
     * ConfigurationSection 中可能包含 Map, List, ConfigurationSection 及任意值
     * 所有值的处理都放在这个方法里循环调用了,
     * 所以参数和返回值都是Any
     * @param data 待转换内容
     * @return 转换结果
     */
    @JvmStatic
    fun toMap(data: Any?): Any? {
        when (data) {
            is ConfigurationSection -> {
                val map = HashMap<String, Any>()
                data.getKeys(false).forEach { key ->
                    toMap(data.get(key))?.let { value -> map[key] = value}
                }
                return map
            }
            is Map<*, *> -> {
                val map = HashMap<String, Any>()
                for ((key, value) in data) {
                    toMap(value)?.let { map[key as String] = it}
                }
                return map
            }
            is List<*> -> {
                val list = ArrayList<Any>()
                for (value in data) {
                    toMap(value)?.let { list.add(it)}
                }
                return list
            }
            else -> {
                return data
            }
        }
    }

    /**
     * ConfigurationSection 转 HashMap
     * @return 转换结果
     */
    @JvmStatic
    fun ConfigurationSection.toMap(): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        this.getKeys(false).forEach { key ->
            toMap(this.get(key))?.let { value -> map[key] = value}
        }
        return map
    }

    /**
     * ConfigurationSection 转 String
     * @param id 转换后呈现的节点ID, 一般可以为this.name(针对MemorySection)
     * @return 转换结果
     */
    @JvmStatic
    fun ConfigurationSection.saveToString(id: String): String {
        val tempConfigSection = YamlConfiguration()
        tempConfigSection.set(id, this)
        return tempConfigSection.saveToString()
    }

    /**
     * String 转 ConfigurationSection
     * @param id 转换前使用的节点ID
     * @return 转换结果
     */
    @JvmStatic
    fun String.loadFromString(id: String): ConfigurationSection? {
        val tempConfigSection = YamlConfiguration()
        tempConfigSection.loadFromString(this)
        return tempConfigSection.getConfigurationSection(id)
    }

    /**
     * File 转 YamlConfiguration
     * @return 转换结果
     */
    @JvmStatic
    fun File.loadConfiguration(): YamlConfiguration {
        return YamlConfiguration.loadConfiguration(this)
    }

    /**
     * ConfigurationSection 合并(后者覆盖前者)
     * @param configSection 用于合并覆盖
     * @return 合并结果
     */
    @JvmStatic
    fun ConfigurationSection.coverWith(configSection: ConfigurationSection): ConfigurationSection {
        // 遍历所有键
        configSection.getKeys(false).forEach { key ->
            // 用于覆盖的值
            val coverValue = configSection.get(key)
            // 原有值
            val value = this.get(key)
            // 如果二者包含相同键
            if (value != null) {
                // 如果二者均为ConfigurationSection
                if (value is ConfigurationSection
                    && coverValue is ConfigurationSection) {
                    // 合并
                    this.set(key, value.coverWith(coverValue))
                } else {
                    // 覆盖
                    this.set(key, coverValue)
                }
            } else {
                // 添加
                this.set(key, coverValue)
            }
        }
        return this
    }
}