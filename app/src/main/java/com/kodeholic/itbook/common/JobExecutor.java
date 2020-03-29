package com.kodeholic.itbook.common;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.kodeholic.itbook.lib.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JobExecutor {
    private List<JobDoer> jobDoers;
    //
    private List<Job>            jobList;
    private HashMap<String, Job> jobHash;

    private Context mContext;
    private Handler mHandler;

    public JobExecutor(Context context, int n) {
        mContext = context.getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper());
        //
        jobList = new ArrayList<>();
        jobHash = new HashMap<>();
        jobDoers= new ArrayList<>();
        for (int i = 0; i < n; i++) {
            jobDoers.add(new JobDoer(i+1));
        }
    }

    /**
     * 일꾼을 시작한다.
     */
    public void start() {
        for (JobDoer jobDoer : jobDoers) {
            jobDoer.begin();
        }
    }

    /**
     * 일꾼을 종료시킨다. (쓸 일 있을까??)
     */
    public void stop() {
        for (JobDoer jobDoer : jobDoers) {
            jobDoer.end();
        }
    }

    /**
     * 일감을 할당한다. (프로듀서 역할)
     * @param job
     * @return
     */
    public Job put(Job job) {
        synchronized (jobHash) {
            Job removed = jobHash.remove(job.getId());
            if (removed != null) {
                jobList.remove(removed);
                removed.cancel();
            }
            jobHash.put(job.getId(), job);
            jobList.add(job);
            jobHash.notifyAll();

            return removed;
        }
    }

    /**
     * 일감을 기다린다
     * @return
     */
    public Job nextOrWait() {
        synchronized (jobHash) {
            if (jobList.size() > 0) {
                Job removed = jobList.remove(0);
                jobHash.remove(removed.getId());
                return removed;
            }

            //기다린다.
            try {
                jobHash.wait();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 작업을 정의한다.
     */
    public static abstract class Job {
        boolean canceled = false;
        public void cancel() {
            canceled = true;
        }
        public boolean isCanceled() {
            return canceled;
        }

        //구현할 것!!
        public abstract String getId();
        public abstract void onJobExecute();
        public abstract void onJobComplete();
    }

    /**
     * 컨슈머를 정의한다.
     */
    public class JobDoer implements Runnable {
        private boolean running;
        private int index;
        public JobDoer(int index) {
            this.index = index;
        }

        //시작한다.
        public void begin() {
            running = true;
            Thread t = new Thread(this);
            t.start();
        }

        //종료한다.
        public void end() {
            running = false;

            //Doer에게 알린다.
            synchronized (jobHash) {
                jobHash.notifyAll();
            }
        }

        //Job을 처리한다. (background --> foreground)
        @Override
        public void run() {
            Log.d("JobDoer", "[" + index + "] start.");

            while (running) {
                Log.d("JobDoer", "[" + index + "] nextOrWait.");
                final Job job = nextOrWait();
                if (job != null) {
                    Log.d("JobDoer", "[" + index + "] execute.");
                    job.onJobExecute();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            job.onJobComplete();
                        }
                    });
                }
            }

            Log.d("JobDoer", "[" + index + "] stop.");
        }
    }
}
