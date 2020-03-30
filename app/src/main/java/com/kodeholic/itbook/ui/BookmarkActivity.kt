package com.kodeholic.itbook.ui

import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.kodeholic.itbook.R
import com.kodeholic.itbook.common.BitmapCacheManager
import com.kodeholic.itbook.common.BookManager
import com.kodeholic.itbook.common.MyIntent
import com.kodeholic.itbook.common.PopupManager
import com.kodeholic.itbook.common.data.BookDetail
import com.kodeholic.itbook.lib.util.JSUtil
import com.kodeholic.itbook.lib.util.Log
import com.kodeholic.itbook.ui.base.IBase
import java.util.ArrayList


class BookmarkActivity: AppCompatActivity(), IBase, View.OnClickListener {
    public val TAG = BookmarkActivity::class.java.simpleName

    companion object {
        private val VIEW_TYPE_LOW  = 0
        private val VIEW_TYPE_MID  = 1
        private val VIEW_TYPE_HIGH = 2
        private val VIEW_TYPE_EDIT = 3
    }

    private lateinit var mContext: Context
    private var mNVList: MutableList<NV> = ArrayList()
    private var mDetail: BookDetail? = null

    private var mNoteViewHolder: ViewHolder? = null
    private var mNewNote: String? = null

    private var mAdapter: BookmarkAdapter? = null
    private var mListView: RecyclerView? = null

    private var pb_loading: ProgressBar? = null
    private var tv_label: TextView? = null
    private var iv_image: ImageView? = null
    private var bt_exit: Button? = null
    private var tv_bookmark: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)

        //variable
        mContext = this;

        //binding views
        pb_loading = findViewById(R.id.pb_loading)
        tv_label = findViewById(R.id.tv_label)
        iv_image = findViewById(R.id.iv_image)
        bt_exit = findViewById(R.id.bt_exit)
        tv_bookmark = findViewById(R.id.tv_bookmark)

        //adapter
        mAdapter = BookmarkAdapter()
        mAdapter?.data  = toArray()

        //list..
        mListView = findViewById(R.id.ll_list)
        mListView?.adapter = mAdapter

        //onclick
        bt_exit?.setOnClickListener(this)
        tv_bookmark?.setOnClickListener(this)

        //LB를 등록한다.
        val filter = IntentFilter()
        filter.addAction(MyIntent.Action.DETAIL_ACTION)
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mControlRecevier, filter)

        //Intent를 체크하고, 정합 오류인 경우 Activity를 종료시킨다.
        onProcess(intent, "onCreate")

        //logging...
        Log.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed()")
        if (checkNoteAndShowDialog()) {
            return
        }
        super.onBackPressed()
    }

    override fun showLoading() {
        Log.d(TAG, "showLoading()")
        runOnUiThread { pb_loading?.visibility = View.VISIBLE }
    }

    override fun hideLoading() {
        Log.d(TAG, "hideLoading()")
        runOnUiThread { pb_loading?.visibility = View.GONE }
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick() - v: " + v?.id)
        when (v?.getId()) {
            R.id.bt_exit -> {
                if (checkNoteAndShowDialog()) {
                    return
                }
                finish()
            }

            R.id.tv_bookmark -> {
                if (!BookManager.getInstance(mContext).isBookmark(mDetail?.getIsbn13())) {
                    BookManager.getInstance(mContext).putBookmark(mDetail, TAG)
                    PopupManager.getInstance(mContext).showToast("BookmarkActivity Added")
                } else {
                    BookManager.getInstance(mContext).delBookmark(mDetail?.getIsbn13(), TAG)
                    PopupManager.getInstance(mContext).showToast("BookmarkActivity Removed")
                }
                updateView("tv_bookmark")
            }
        }
    }

    private fun onProcess(intent: Intent?, f: String?) {
        MyIntent.show(TAG, f, intent)

        //intent 유효성 여부 검사
        if (intent == null) {
            Log.e(TAG, "onProcess(). Intent is null!!")
            return
        }

        if (MyIntent.Action.DETAIL_ACTION != intent.action) {
            Log.e(TAG, "onProcess(). Invalid Action!! - " + intent.action!!)
            return
        }

        try {
            mDetail = JSUtil.json2Object(intent.getStringExtra(MyIntent.Extra.BOOK_DETAIL), BookDetail::class.java)
            mNewNote = mDetail?.getNote()
            synchronized(mNVList) {
                mNVList.clear()
                mNVList.add(NV("Title", mDetail?.getTitle(), VIEW_TYPE_LOW))
                mNVList.add(NV("SubTitle", mDetail?.getSubTitle(), VIEW_TYPE_LOW))
                mNVList.add(NV("authors", mDetail?.getAuthors(), VIEW_TYPE_LOW))
                mNVList.add(NV("publisher", mDetail?.getPublisher(), VIEW_TYPE_LOW))
                mNVList.add(NV("language", mDetail?.getLanguage(), VIEW_TYPE_LOW))
                mNVList.add(NV("isbn10", mDetail?.getIsbn10(), VIEW_TYPE_LOW))
                mNVList.add(NV("isbn13", mDetail?.getIsbn13(), VIEW_TYPE_LOW))
                mNVList.add(NV("pages", "" + mDetail?.getPages(), VIEW_TYPE_LOW))
                mNVList.add(NV("year", "" + mDetail?.getYear(), VIEW_TYPE_LOW))
                mNVList.add(NV("rating", "" + mDetail?.getRating(), VIEW_TYPE_LOW))
                mNVList.add(NV("desc", mDetail?.getDesc(), VIEW_TYPE_HIGH))
                mNVList.add(NV("price", mDetail?.getPrice(), VIEW_TYPE_LOW))
                mNVList.add(NV("image", mDetail?.getImage(), VIEW_TYPE_LOW))
                mNVList.add(NV("url", mDetail?.getUrl(), VIEW_TYPE_LOW))
                mNVList.add(NV("note", mDetail?.getNote(), VIEW_TYPE_EDIT))
            }

            //View를 갱신한다.
            updateView("onProcess")
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        if (mDetail == null) {
            Log.e(TAG, "onProcess() - mDetail is NULL! stop activity!!")
            finish()
        }

        return
    }

    private fun toArray(): Array<NV> {
        synchronized(mNVList) {
            return mNVList.toTypedArray()
        }
    }

    private fun updateView(f: String) {
        Log.d(TAG, "updateView() - f: $f")
        runOnUiThread { updateView() }
    }

    private fun updateView() {
        if (mDetail == null) {
            return
        }
        tv_label?.setText(mDetail?.getTitle())

        //북마크 버튼 label
        val isBookmark = BookManager.getInstance(mContext).isBookmark(mDetail?.getIsbn13())
        Log.d(TAG, "updateView() - isBookmark: $isBookmark")
        if (!isBookmark) {
            tv_bookmark?.setText("Add BookmarkActivity")
        } else {
            tv_bookmark?.setText("Remove BookmarkActivity")
        }

        //이미지를 view에 붙인다.
        BitmapCacheManager.getInstance(mContext).loadBitmap(
                mDetail?.getImage(),
                iv_image,
                TAG)

        //작성중인 Note를 복원한다.
        if (mNoteViewHolder?.ed_input != null) {
            mNewNote = mNoteViewHolder?.ed_input?.getText().toString()
        }
        mAdapter?.data = toArray()
        mAdapter?.notifyDataSetChanged()
    }

    private val mControlRecevier = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onProcess(intent, "onReceive")
        }
    }

    /**
     * define RecyclerView adapter
     */
    inner class BookmarkAdapter : RecyclerView.Adapter<ViewHolder>() {
        var data: Array<NV>? = null

//        fun setData(data: Array<NV>) {
//            this.data = data
//        }

        override fun getItemViewType(position: Int): Int {
            // loader can't be at position 0
            // loader can only be at the last position
            if (!data.isNullOrEmpty()) {
                return data!![position].viewType
            }

            return VIEW_TYPE_HIGH
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            Log.d(TAG, "onCreateViewHolder() - viewType: $viewType")
            //when... else...
            val layoutId = when (viewType) {
                VIEW_TYPE_LOW -> R.layout.list_item_name_value_low
                VIEW_TYPE_MID -> R.layout.list_item_name_value_mid
                VIEW_TYPE_HIGH -> R.layout.list_item_name_value_high
                else -> R.layout.list_item_name_value_edit
            }

            //inflate layout by viewtype
            val itemView = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

            //create viewholder
            val viewHolder = ViewHolder(itemView, viewType)
            if (viewHolder.viewType == VIEW_TYPE_EDIT) {
                mNoteViewHolder = viewHolder
            }

            return viewHolder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Log.d(TAG, "onBindViewHolder() - data.size: " + data?.size)
            val item = data!![position]
            holder.tv_name?.setText(item.name)
            holder.tv_value?.setText(item.value)
            holder.ed_input?.setText(mNewNote)
        }

        override fun getItemCount(): Int = data?.size ?: 0
    }

    inner class ViewHolder(itemView: View, var viewType: Int) : RecyclerView.ViewHolder(itemView) {
        var tv_name: TextView? = null
        var tv_value: TextView? = null
        var ed_input: EditText? = null

        init {
            this.tv_name  = itemView.findViewById(R.id.tv_name)
            this.tv_value = itemView.findViewById(R.id.tv_value)
            this.ed_input = itemView.findViewById(R.id.ed_input)
        }
    }

    private fun checkNoteAndShowDialog():Boolean {
        if (mNoteViewHolder?.ed_input != null) {
            val newNote = mNoteViewHolder?.ed_input!!.getText().toString()
            val oldNote = if (mDetail?.getNote() != null) mDetail?.getNote() else ""
            Log.d(TAG, "newNote: $newNote")
            Log.d(TAG, "oldNote: $oldNote")
            if (newNote != oldNote) {
                showDialog(mDetail?.isbn13, newNote)
                return true
            }
        }

        return false
    }

    private fun showDialog(isbn13:String?, note:String?) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Notes have changed. Do you want to save?")
        builder.setPositiveButton("Yes"
        ) { dialog, which ->
            BookManager.getInstance(mContext).saveNote(isbn13, note, TAG)
            finish()
        }
        builder.setNegativeButton("No",
                object: DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which:Int) {
                        finish()
                    }
                })
        builder.show()
    }
}

class NV(var name: String?,
         var value: String?,
         var viewType: Int)
{ }


