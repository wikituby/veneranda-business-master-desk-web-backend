import org.objectweb.asm.*;

import java.io.InputStream;
import java.nio.file.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * Hot-patches the hospital runner JAR so "Dispensed, no money received" works
 * without a full Quarkus rebuild:
 * 1) completeSale: allow visitId (prescription already tagged) instead of requiring patientId
 * 2) tryRecordPharmacyDispensePayment: skip creating a payment when notes say "no payment"
 */
public class PatchOtcNoPay {
    static final String REQ = "org/example/pharmacy/otc/services/payloads/requests/OtcSaleCompleteRequest";
    static final String SVC = "org/example/pharmacy/otc/services/OtcPharmacySaleService";
    static final String PAY = "org/example/finance/payments/cash/services/PaymentService";
    static final String ERR = "Select a client to tag unpaid balance, or pay the full amount.";

    public static void main(String[] args) throws Exception {
        Path in = Path.of(args[0]);
        Path out = Path.of(args[1]);
        Path tmp = Files.createTempFile("otc-patched-", ".jar");
        Files.deleteIfExists(tmp);

        try (JarFile jar = new JarFile(in.toFile());
             JarOutputStream jos = new JarOutputStream(Files.newOutputStream(tmp))) {
            jar.stream().forEach(entry -> {
                try {
                    byte[] bytes;
                    try (InputStream is = jar.getInputStream(entry)) {
                        bytes = is.readAllBytes();
                    }
                    String name = entry.getName();
                    if ((SVC + ".class").equals(name)) {
                        bytes = patchService(bytes);
                        System.out.println("patched " + name);
                    } else if ((PAY + ".class").equals(name)) {
                        bytes = patchPayment(bytes);
                        System.out.println("patched " + name);
                    }
                    JarEntry je = new JarEntry(name);
                    je.setTime(entry.getTime());
                    jos.putNextEntry(je);
                    jos.write(bytes);
                    jos.closeEntry();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        Files.move(tmp, out, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("OK " + out.toAbsolutePath());
    }

    static byte[] patchService(byte[] input) {
        ClassReader cr = new ClassReader(input);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex) {
                MethodVisitor mv = super.visitMethod(access, name, desc, sig, ex);
                if (!"completeSale".equals(name)) return mv;
                return new MethodVisitor(Opcodes.ASM9, mv) {
                    Label pendingNonNullTarget;
                    boolean maybeSelectClient;

                    @Override
                    public void visitJumpInsn(int opcode, Label label) {
                        maybeSelectClient = (opcode == Opcodes.IFNONNULL);
                        pendingNonNullTarget = maybeSelectClient ? label : null;
                        super.visitJumpInsn(opcode, label);
                    }

                    @Override
                    public void visitLdcInsn(Object value) {
                        if (maybeSelectClient
                                && pendingNonNullTarget != null
                                && ERR.equals(value)) {
                            // if (request.visitId != null) skip the unpaid-client error
                            mv.visitVarInsn(Opcodes.ALOAD, 1);
                            mv.visitFieldInsn(Opcodes.GETFIELD, REQ, "visitId", "Ljava/lang/Long;");
                            mv.visitJumpInsn(Opcodes.IFNONNULL, pendingNonNullTarget);
                        }
                        maybeSelectClient = false;
                        pendingNonNullTarget = null;
                        super.visitLdcInsn(value);
                    }

                    @Override
                    public void visitInsn(int opcode) {
                        maybeSelectClient = false;
                        super.visitInsn(opcode);
                    }

                    @Override
                    public void visitVarInsn(int opcode, int var) {
                        if (!(maybeSelectClient && opcode == Opcodes.ALOAD)) {
                            // keep maybeSelectClient only across aload_1-style before getfield visitId insert
                        }
                        if (opcode != Opcodes.ALOAD || var != 1) {
                            // allow pattern: ifnonnull; aload; getfield visitId we insert; ldc
                        }
                        super.visitVarInsn(opcode, var);
                    }
                };
            }
        }, 0);
        return cw.toByteArray();
    }

    static byte[] patchPayment(byte[] input) {
        ClassReader cr = new ClassReader(input);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex) {
                MethodVisitor mv = super.visitMethod(access, name, desc, sig, ex);
                if (!"tryRecordPharmacyDispensePayment".equals(name)) return mv;
                return new MethodVisitor(Opcodes.ASM9, mv) {
                    boolean inserted;

                    @Override
                    public void visitCode() {
                        super.visitCode();
                        if (inserted) return;
                        inserted = true;
                        // if (template != null && template.notes != null
                        //     && template.notes.toLowerCase().contains("no payment")) return false;
                        Label cont = new Label();
                        mv.visitVarInsn(Opcodes.ALOAD, 3); // template
                        mv.visitJumpInsn(Opcodes.IFNULL, cont);
                        mv.visitVarInsn(Opcodes.ALOAD, 3);
                        mv.visitFieldInsn(Opcodes.GETFIELD,
                                "org/example/finance/payments/cash/services/payloads/requests/PaymentRequest",
                                "notes", "Ljava/lang/String;");
                        mv.visitJumpInsn(Opcodes.IFNULL, cont);
                        mv.visitVarInsn(Opcodes.ALOAD, 3);
                        mv.visitFieldInsn(Opcodes.GETFIELD,
                                "org/example/finance/payments/cash/services/payloads/requests/PaymentRequest",
                                "notes", "Ljava/lang/String;");
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "toLowerCase",
                                "()Ljava/lang/String;", false);
                        mv.visitLdcInsn("no payment");
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "contains",
                                "(Ljava/lang/CharSequence;)Z", false);
                        mv.visitJumpInsn(Opcodes.IFEQ, cont);
                        mv.visitInsn(Opcodes.ICONST_0);
                        mv.visitInsn(Opcodes.IRETURN);
                        mv.visitLabel(cont);
                    }
                };
            }
        }, 0);
        return cw.toByteArray();
    }
}
