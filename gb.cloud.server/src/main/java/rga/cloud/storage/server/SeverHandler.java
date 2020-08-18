package rga.cloud.storage.server;

import rga.cloud.storage.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.TreeMap;

public class SeverHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            String SERVER_STORAGE_PATH = "server_storage/";
            if (msg instanceof FileMessage) {
                FileMessage fileMessage = (FileMessage) msg;
                Files.write(Paths.get
                        (SERVER_STORAGE_PATH + fileMessage.getFileName()),
                        fileMessage.getFileData(), StandardOpenOption.CREATE);

            } else if (msg instanceof FileRequest) {
                FileRequest fileRequest = (FileRequest) msg;
                if (Files.exists(Paths.get(SERVER_STORAGE_PATH + fileRequest.getFileName()))) {
                    FileMessage fileMessage = new FileMessage(Paths.get
                            (SERVER_STORAGE_PATH + fileRequest.getFileName()));
                    ctx.writeAndFlush(fileMessage);
                }

            } else if (msg instanceof DeleteMessage) {
                DeleteMessage deleteMessage = (DeleteMessage) msg;
                Files.delete(Paths.get(SERVER_STORAGE_PATH + deleteMessage.getDeletingFileName()));

            } else if (msg instanceof UpdateMessage) {
                Files.walkFileTree(Paths.get(SERVER_STORAGE_PATH),
                        new SimpleFileVisitor<Path>() {
                    public FileVisitResult visitFile (Path file, BasicFileAttributes attrs) {
                        TreeMap<String, Long> treeMap = new TreeMap<>();
                        String fileName = file.getFileName().toString();
                        Long fileSize = (long) Math.ceil(file.toFile().length() / 1024.0);
                        treeMap.put(fileName, fileSize);
                        UpdateServerStorageMessage updateServerStorageMessage =
                                new UpdateServerStorageMessage(treeMap);
                        ctx.writeAndFlush(updateServerStorageMessage);
                        return FileVisitResult.CONTINUE;
                    }
                });

            } else if (msg instanceof AuthorizationMessage) {
                AuthorizationMessage message = (AuthorizationMessage) msg;
                AuthorizationService.connect();
                if (AuthorizationService.checkUser(message.getLogin())) {
                    if (AuthorizationService.checkUserPassword(message.getLogin(),
                            message.getPassword())) {
                        ctx.writeAndFlush("This user is successfully checked." + message.getLogin());
                    } else {
                        ctx.writeAndFlush("There is incorrect username or password. Please try again.");
                    }
                } else {
                    ctx.writeAndFlush("There is incorrect username or password. Please try again.");
                }

            } else if (msg instanceof RegistrationMessage) {
                RegistrationMessage message = (RegistrationMessage) msg;
                AuthorizationService.connect();
                if (AuthorizationService.checkUser(message.getLogin())) {
                    ctx.writeAndFlush("There is an already existing user. Please try again.");
                } else {
                    AuthorizationService.addNewUser(message.getLogin(), message.getPassword());
                    ctx.writeAndFlush("Congratulations! Registration is successfully completed.");
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
