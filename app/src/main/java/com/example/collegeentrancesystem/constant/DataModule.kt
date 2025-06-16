package com.example.collegeentrancesystem.constant

//省份模型类
data class Province(
    val name: String
)

//科目模型类
data class Subject(
    val name: String,
    var isSelected: Boolean = false
)

class DataModule {
    companion object {
        //获取所有科目列表
        fun getAllSubjects(): List<Subject> {
            return listOf(
                Subject("物理"),
                Subject("历史"),
                Subject("政治"),
                Subject("化学"),
                Subject("地理"),
                Subject("生物")
            )
        }
    }
}

class YearModule{
    companion object{
        //可查询年份
        fun getAllYears(): List<String>{
            return listOf(
                "2025",
                "2024",
                "2023",
                "2022",
                "2021",
                "2020",
                "2019",
                "2018",
                "2017"
            )
        }
    }
}
