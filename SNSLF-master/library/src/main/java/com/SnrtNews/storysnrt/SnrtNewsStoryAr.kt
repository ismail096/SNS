package com.SnrtNews.storysnrt


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.SnrtNews.storysnrt.callbacks.ProgressTimeWatcher
import com.SnrtNews.storysnrt.callbacks.StoryCallback
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.progress_story_view.view.*

@SuppressLint("ViewConstructor")
class SnrtNewsStoryAr(
        context: Context,
        private val passedInContainerView: ViewGroup,
        private val storyUser: List<List<StoryItem>>,
        private var storyCallback: StoryCallback,
        @DrawableRes private var mProgressDrawable: Int = R.drawable.white_lightgrey_drawable
) : ConstraintLayout(context) {
    private var currentlyShownIndex = 0
    private var index = 0
    private lateinit var currentView: StoryItem
    private var libSliderViewList = mutableListOf<MyProgressBar>()
    private lateinit var view: View
    private var pausedState: Boolean = false
    lateinit var gestureDetector: GestureDetector
    private var exoPlayer: SimpleExoPlayer? = null
    private var videoListener : Player.EventListener? = null
    private fun View.getActivity(): Activity = context as Activity



    private fun StoryItem.fillView(context: Context): View {

        return when (this) {
            is StoryItem.RemoteImage -> ImageView(context)
            is StoryItem.Video -> LayoutInflater.from(
                    context
            ).inflate(R.layout.video_player, null)

            is StoryItem.CustomView -> customView
            is StoryItem.CustomLayout -> LayoutInflater.from(
                    context
            ).inflate(layout, null)
        }
    }

    private fun init() {
        view.linearProgressIndicatorLay.removeAllViews()
        storyUser.get(index).forEachIndexed {
            indexk, sliderView ->
            val myProgressBar = MyProgressBar(
                    context,
                    indexk,
                    sliderView.durationInSeconds,
                    object : ProgressTimeWatcher {
                        override fun onEnd(indexFinished: Int) {
                            next()
                        }
                    },
                    mProgressDrawable
            )
            libSliderViewList.add(myProgressBar)

            view.linearProgressIndicatorLay.addView(myProgressBar)
        }
    }

    fun callPause(pause: Boolean) {
        try {
            if (pause) {
                if (!pausedState) {
                    this.pausedState = !pausedState
                    pause(false)
                }
            } else {
                if (pausedState) {
                    this.pausedState = !pausedState
                    resume()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initView() {
        view = View.inflate(
                context,
                R.layout.progress_story_viewar, this
        )
        val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )

        gestureDetector = GestureDetector(context, SingleTapConfirm())

        val touchListener = object : OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (gestureDetector.onTouchEvent(event)) {
                    // single tap
                    if (v?.id == view.view1.close.id) {
                        getActivity().finish()
                        getActivity().overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_bottom);
                    }
                    else  if (v?.id == view.rightLay.id) {
                        pausedState = false
                        prev()
                    } else if (v?.id == view.leftLay.id) {
                        pausedState = false
                        next()
                    }

                    return true
                } else {
                    return when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            callPause(true)
                            true
                        }

                        MotionEvent.ACTION_UP -> {
                            callPause(false)
                            true
                        }
                        else -> false
                    }
                }
            }
        }

        view.leftLay.setOnTouchListener(touchListener)
        view.rightLay.setOnTouchListener(touchListener)
        view.view1.close.setOnTouchListener(touchListener)

        this.layoutParams = params
        passedInContainerView.addView(this)
    }

    private fun show() {
        view.loaderProgressbar.visibility = View.GONE

        if (currentlyShownIndex != 0) {
            for (i in 0..0.coerceAtLeast(currentlyShownIndex - 1)) {
                libSliderViewList[i].progress = 100
                libSliderViewList[i].cancelProgress()
            }
        }

        if (currentlyShownIndex != libSliderViewList.size - 1) {
            for (i in currentlyShownIndex + 1 until libSliderViewList.size) {
                libSliderViewList[i].progress = 0
                libSliderViewList[i].cancelProgress()
            }
        }
        if (storyUser.get(index).size == 1) {
            currentlyShownIndex = 0
        }
        currentView = storyUser[index][currentlyShownIndex]


        libSliderViewList[currentlyShownIndex].startProgress()


        when (currentView) {
            is StoryItem.RemoteImage -> {
                pause(true)
                loadRemoteImage((currentView as StoryItem.RemoteImage))
            }
            is StoryItem.Video -> {
                pause(false)
                playVideo(
                        currentlyShownIndex,
                        (currentView as StoryItem.Video)
                )
            }
        }
        storyCallback.onNextCalled(currentView, currentlyShownIndex)

        view.currentlyDisplayedView.removeAllViews()
        view.currentlyDisplayedView.addView(currentView.view)
        val params = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT, 1f
        )

        if (currentView.view is ImageView) {
            (currentView.view as ImageView).scaleType = ImageView.ScaleType.CENTER_CROP
        }
        currentView.view.layoutParams = params
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadRemoteImage(remoteImage: StoryItem.RemoteImage) {
        Picasso.get()
                .load(remoteImage.imageUrl)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(remoteImage.view as ImageView, object : Callback {
                    override fun onSuccess() {
                        resume()
                    }

                    override fun onError(e: Exception?) {
                        e?.printStackTrace()
                    }
                })
        Picasso.get()
                .load(remoteImage.AvatarJournal)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(view.user_image as ImageView, object : Callback {
                    override fun onSuccess() {
                        resume()
                    }

                    override fun onError(e: Exception?) {
                        e?.printStackTrace()
                    }
                })

        val typeface = ResourcesCompat.getFont(context,R.font.exbold)
        val typeface2 = ResourcesCompat.getFont(context,R.font.bold)
        val typeface3 = ResourcesCompat.getFont(context,R.font.arbold)

        view.namejour.text = remoteImage.NameJournal
        val longapi : String = remoteImage.LongApi

        if(longapi.equals("fr"))
        {
            view.namejour.typeface = typeface
        }
        else if(longapi.equals("ar"))
        {
            view.namejour.typeface = typeface3
        }



        view.TitleStory.text = remoteImage.TitleStory
        if(longapi.equals("fr"))
        {
            view.TitleStory.typeface = typeface2
        }
        else if(longapi.equals("ar"))
        {
            view.TitleStory.typeface = typeface3
        }


        view.TimePub.text = remoteImage.TimePublication
        if(longapi.equals("fr"))
        {
            view.TimePub.typeface = typeface
        }
        else if(longapi.equals("ar"))
        {
            view.TimePub.typeface  = typeface3
        }







    }

    fun start(UserPosition : Int)
    {

        index = UserPosition
        initView()
        init()
        storyUser.get(index).map { it.view = it.fillView(this.context) }
        show()
    }

    private fun playVideo(
            index: Int,
            videoStoryItem: StoryItem.Video
    ) {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = SimpleExoPlayer.Builder(context).build()
        videoListener = object : Player.EventListener{
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    editDurationAndResume(index, exoPlayer!!.duration.toInt() / 1000)
                    exoPlayer?.removeListener(this)
                }
            }
        }
        exoPlayer?.addListener(videoListener as Player.EventListener)

        (videoStoryItem.view as PlayerView).player = exoPlayer
        exoPlayer?.playWhenReady = true
        exoPlayer?.seekTo(0, 0)

        val uri = Uri.parse(videoStoryItem.videoUrl)
        val defaultHttpDataSourceFactory = DefaultHttpDataSourceFactory("exoplayer")
        val mediaSource = ProgressiveMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(uri)
        exoPlayer?.prepare(mediaSource, true, false)

        Picasso.get()
                .load(videoStoryItem.AvatarJournal)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(view.user_image as ImageView, object : Callback {
                    override fun onSuccess() {
                        resume()
                    }

                    override fun onError(e: Exception?) {
                        e?.printStackTrace()
                    }
                })
        view.namejour.text = videoStoryItem.NameJournal;
        view.TimePub.text = videoStoryItem.TimePublication;
        view.TitleStory.text = videoStoryItem.TitleStory;


    }

    fun editDurationAndResume(index: Int, newDurationInSecons: Int) {
        view.loaderProgressbar.visibility = View.GONE
        libSliderViewList[index].editDurationAndResume(newDurationInSecons)
    }

    fun pause(withLoader: Boolean) {
        if (withLoader) {
            view.loaderProgressbar.visibility = View.VISIBLE
        }
        if (storyUser.get(index).size == 1) {
            currentlyShownIndex = 0
        }
        if (currentlyShownIndex == storyUser.get(index).size) return
        libSliderViewList[currentlyShownIndex].pauseProgress()
        if (storyUser.get(index)[currentlyShownIndex] is StoryItem.Video) {
            exoPlayer?.playWhenReady = false
        }
    }

    fun resume() {
        view.loaderProgressbar.visibility = View.GONE
        if (storyUser.get(index).size == 1) {
            currentlyShownIndex = 0
        }
        if (currentlyShownIndex == storyUser.get(index).size) return
        libSliderViewList[currentlyShownIndex].resumeProgress()
        if (storyUser.get(index)[currentlyShownIndex] is StoryItem.Video) {
            exoPlayer?.playWhenReady = true
        }
    }

    fun next() {
        try {
            if (currentView == storyUser.get(index)[currentlyShownIndex])
            {
                currentlyShownIndex++
                if (storyUser.get(index).size <= currentlyShownIndex)
                {
                    if(storyUser.size > index+1)
                    {
                        nextUser()
                    }
                    else
                    {
                        finish()
                        return
                    }

                }
                else
                {
                    show()
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            finish()
        }
    }

    fun nextUser()
    {
        index++
        currentlyShownIndex =0
        libSliderViewList.clear()
        init()
        storyUser.get(index).map { it.view = it.fillView(this.context) }
        show()

    }




    fun prevUser()
    {
        index--
        currentlyShownIndex =0
        libSliderViewList.clear()
        init()
        storyUser.get(index).map { it.view = it.fillView(this.context) }
        show()

    }


    fun prevUsers()
    {
        index = 0
        currentlyShownIndex =0
        libSliderViewList.clear()
        init()
        storyUser.get(index).map { it.view = it.fillView(this.context) }
        show()

    }

    private fun finish() {
        storyCallback.done()
        for (progressBar in libSliderViewList) {
            getActivity().finish()
            getActivity().overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_bottom);
        }
    }

    fun prev() {
        try {
            if (currentView == storyUser.get(index)[currentlyShownIndex]) {
                currentlyShownIndex -= 1
                if (0 > currentlyShownIndex)
                {
                    if(index == 0)
                    {
                        prevUsers()
                    }
                    else
                    {
                        prevUser()
                    }

                }
                else
                {
                    show()
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            currentlyShownIndex -= 1
        }
    }

    fun release(){
        exoPlayer?.stop()
        exoPlayer?.release()
    }

    private inner class SingleTapConfirm : SimpleOnGestureListener() {

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            return true
        }
    }
}