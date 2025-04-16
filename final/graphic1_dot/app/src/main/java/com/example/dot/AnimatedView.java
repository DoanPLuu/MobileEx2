package com.example.dot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import java.util.Random;

public class AnimatedView extends View {
    private static final int BALL_COUNT = 5;
    private static final int BALL_RADIUS = 50;
    private static final int SPEED = 10;
    private Paint paint;
    private Ball[] balls;
    private Random random;

    public AnimatedView(Context ctx) {
        super(ctx);
        paint = new Paint();
        random = new Random();
        balls = new Ball[BALL_COUNT];

        for (int i = 0; i < BALL_COUNT; i++) {
            balls[i] = new Ball(random.nextInt(800), random.nextInt(600), SPEED);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Ball ball : balls) {
            paint.setColor(ball.color);
            canvas.drawCircle(ball.x, ball.y, BALL_RADIUS, paint);
            ball.update(getWidth(), getHeight());
        }
        postInvalidateDelayed(30);
    }

    private class Ball {
        int x, y;
        int dx, dy;
        int color;

        Ball(int x, int y, int speed) {
            this.x = x;
            this.y = y;
            this.dx = random.nextInt(speed * 2) - speed;
            this.dy = random.nextInt(speed * 2) - speed;
            this.color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }

        void update(int width, int height) {
            x += dx;
            y += dy;

            if (x - BALL_RADIUS < 0 || x + BALL_RADIUS > width) {
                dx = -dx;
            }
            if (y - BALL_RADIUS < 0 || y + BALL_RADIUS > height) {
                dy = -dy;
            }
        }
    }
}
