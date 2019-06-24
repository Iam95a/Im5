package com.example.im5;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.chen.im.common.constant.Constant;
import com.chen.im.common.dto.User;
import com.chen.im.common.protobuf.RequestMessageProto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.collections4.MapUtils;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class MainActivity extends AppCompatActivity {
    public static Channel channel;

    public static User user;

    public static EventLoopGroup group;

    public static ExecutorService service;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_connect) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("警告");
            builder.setMessage("世界上最遥远的距离是没有网");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    service.submit(new Runnable() {
//                            @Override
//                            public void run() {
                    try {
                        if (group == null) {
                            group = new NioEventLoopGroup();
                        }
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    Bootstrap b = new Bootstrap();
                                    b.group(group)
                                            .channel(NioSocketChannel.class)
                                            .remoteAddress(new InetSocketAddress("192.168.1.24", 8888))
                                            .handler(new ChannelInitializer<SocketChannel>() {
                                                @Override
                                                protected void initChannel(SocketChannel sc) throws Exception {
                                                    sc.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                                                    sc.pipeline().addLast(new ProtobufDecoder(RequestMessageProto.RequestMessage.getDefaultInstance()));
                                                    sc.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                                                    sc.pipeline().addLast(new ProtobufEncoder());
                                                    sc.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                                                        private void sendLoginRequest(ChannelHandlerContext ctx) {
                                                            RequestMessageProto.RequestMessage.Builder builder = RequestMessageProto.RequestMessage.newBuilder();
                                                            builder.setCommand(Constant.CMD_LOGIN);
                                                            RequestMessageProto.RequestMessage.User.Builder userBuilder = RequestMessageProto.RequestMessage.User.newBuilder();
                                                            userBuilder.setNickname("chen");
                                                            userBuilder.setPassword("123456");
                                                            builder.setUser(userBuilder.build());
                                                            ctx.writeAndFlush(builder.build());
                                                        }

                                                        @Override
                                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
//                                                            System.out.println("nihao");
                                                            channel = ctx.channel();
                                                            sendLoginRequest(ctx);
                                                        }

                                                        @Override
                                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                                            if (msg instanceof RequestMessageProto.RequestMessage) {
                                                                RequestMessageProto.RequestMessage requestMessage = (RequestMessageProto.RequestMessage) msg;
                                                                String command = requestMessage.getCommand();
                                                                System.out.println(command);
                                                                if (command.equals(Constant.CMD_LOGIN)) {
                                                                    Map<String, String> map = requestMessage.getParamsMap();
                                                                    if (MapUtils.isNotEmpty(map)) {
                                                                        String userId = map.get("userId");
                                                                        user = new User();
                                                                        user.setUserId(Long.valueOf(userId));
                                                                        user.setNickname("chen");
                                                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                                        builder.setTitle("确认");
                                                                        builder.setMessage("您的用户id是" + userId);
                                                                        builder.setPositiveButton("是", null);
                                                                        builder.setNegativeButton("否", null);
                                                                        builder.show();
                                                                    }
                                                                }
                                                            } else {
                                                                System.out.println(msg);
                                                            }

                                                        }


                                                    });
                                                }
                                            });
                                    ChannelFuture f = b.connect().sync();
                                    f.channel().closeFuture().sync();
                                } catch (Exception e) {

                                }

                            }


                        };
                        Thread thread = new Thread(runnable);
                        thread.start();
                    } catch (Exception e) {

                    }

                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.out.println("点了取消");
                }
            });
            //一样要show
            builder.show();
//            Toast toast = Toast.makeText(this, "要显示的内容", Toast.LENGTH_LONG).show();

            return true;
        }

        if(id==R.id.dis){
            System.out.println(channel);
            return true;
        }

//        if(id==){
//
//        }
        return super.onOptionsItemSelected(item);
    }
}
