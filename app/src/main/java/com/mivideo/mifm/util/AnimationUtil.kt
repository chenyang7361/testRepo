package com.mivideo.mifm.util

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import rx.Observable
import rx.lang.kotlin.BehaviorSubject

/**
 * Created by aaron on 2017/3/20.
 */
class AnimationUtil {

    companion object {
        val ANIMATE_DURATION = 450L

        fun loadAnimationToView(animView: View, id: Int) : Observable<Boolean> {
            val subject = BehaviorSubject<Boolean>()

            val animation = AnimationUtils.loadAnimation(animView.context, id)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationStart(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    subject.onNext(true)
                    subject.onCompleted()
                }
            })
            animView.startAnimation(animation)

            return subject
        }

        fun animateAlphaIn(view: View): Animator {
            val animator = ObjectAnimator.ofFloat(view, "alpha",
                    0f, 1f)
            animator.interpolator = DecelerateInterpolator()
            animator.duration = ANIMATE_DURATION
            animator.start()
            view.visibility = View.VISIBLE
            return animator
        }

        fun animateAlphaOut(view: View): Animator {
            val animator = ObjectAnimator.ofFloat(view, "alpha",
                    1f, 0f)
            animator.interpolator = DecelerateInterpolator()
            animator.duration = ANIMATE_DURATION
            animator.addListener(ViewGoneAnimatorListener(view))
            animator.start()
            return animator
        }

        fun animateTranslationYIn(view: View, from: Float, to: Float): Animator {
            val animator = ObjectAnimator.ofFloat(view, "translationY",
                    from, to)
            animator.interpolator = DecelerateInterpolator()
            animator.duration = ANIMATE_DURATION
            animator.start()
            view.visibility = View.VISIBLE
            return animator
        }

        fun animateTranslationYOut(view: View, from: Float, to: Float): Animator {
            val animator = ObjectAnimator.ofFloat(view, "translationY",
                    from, to)
            animator.interpolator = DecelerateInterpolator()
            animator.duration = ANIMATE_DURATION
            animator.addListener(ViewGoneAnimatorListener(view))
            animator.start()
            return animator
        }

    }
}

class ViewGoneAnimatorListener(private val mView: View?) : Animator.AnimatorListener {

    override fun onAnimationCancel(animator: Animator) {
    }

    override fun onAnimationEnd(animator: Animator) {
        if (mView != null) {
            mView.visibility = View.GONE
        }
    }

    override fun onAnimationRepeat(animator: Animator) {
    }

    override fun onAnimationStart(animator: Animator) {
    }

}
