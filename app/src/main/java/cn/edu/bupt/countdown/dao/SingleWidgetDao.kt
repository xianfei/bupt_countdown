package cn.edu.bupt.countdown.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cn.edu.bupt.countdown.bean.SingleAppWidgetBean

@Dao
interface SingleWidgetDao {

    @Insert
    fun insert(widget: SingleAppWidgetBean)

    @Update
    fun update(widget: SingleAppWidgetBean)

    @Query("delete from singleappwidgetbean where id = :id")
    fun delete(id: Int)

    @Query("select * from singleappwidgetbean")
    fun getAll(): List<SingleAppWidgetBean>

    @Query("select * from singleappwidgetbean where eventId = :eventId")
    fun getByEvent(eventId: Int): List<SingleAppWidgetBean>
}