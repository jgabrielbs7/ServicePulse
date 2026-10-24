package com.helpdesk.config;

import com.helpdesk.model.*;
import com.helpdesk.repository.ChamadoRepository;
import com.helpdesk.repository.ClienteRepository;
import com.helpdesk.repository.MotivoChamadoRepository;
import com.helpdesk.repository.UsuarioRepository;
import com.helpdesk.service.ChamadoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;
import java.util.Random;

/**
 * Banco vazio:
 *  - app.seed-demo-data=true  -> cria usuários, clientes e chamados de EXEMPLO (somente para testes locais);
 *  - app.seed-demo-data=false -> cria apenas o primeiro administrador, a partir de BOOTSTRAP_ADMIN_*.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final ChamadoRepository chamados;
    private final MotivoChamadoRepository motivos;
    private final PasswordEncoder encoder;

    @Value("${app.seed-demo-data:false}")
    private boolean seed;

    @Value("${app.bootstrap-admin.name:Admin}")
    private String adminNome;

    @Value("${app.bootstrap-admin.email:admin@helpdesk.com}")
    private String adminEmail;

    @Value("${app.bootstrap-admin.password:SenhaSegura123!}")
    private String adminSenha;

    private static final String[] DESCRICOES = {
            "Usuários relatam indisponibilidade desde o início da manhã.",
            "Sistema apresenta erro ao emitir notas fiscais.",
            "Equipamento apresentando falha e reiniciando sozinho.",
            "Conexão instável com perda de pacotes constante.",
            "Impressora do financeiro não responde na rede.",
            "Colaborador sem acesso após troca de senha."
    };

    @Override
    @Transactional
    public void run(String... args) {
        seedMotivos();
        if (usuarios.count() > 0) return;
        if (!seed) {
            criarAdministradorInicial();
            return;
        }

        usuarios.save(novoUsuario("Administrador", "admin@helpdesk.com", "admin123", Perfil.ADMIN));
        Usuario t1 = usuarios.save(novoUsuario("Carlos Técnico", "tecnico@helpdesk.com", "tecnico123", Perfil.TECNICO));
        Usuario t2 = usuarios.save(novoUsuario("Marina Técnica", "marina@helpdesk.com", "tecnico123", Perfil.TECNICO));

        List<Cliente> base = new ArrayList<>();
        base.add(novoCliente("Construtora Horizonte Ltda", "12.345.678/0001-90"));
        base.add(novoCliente("Clínica Vida Plena", "23.456.789/0001-01"));
        base.add(novoCliente("Transportes Rápido Sul", "34.567.890/0001-12"));
        base.add(novoCliente("Escritório Almeida & Rocha", "45.678.901/0001-23"));
        base.add(novoCliente("Mercado Bom Preço", "56.789.012/0001-34"));
        base.add(novoCliente("Colégio Nova Geração", "67.890.123/0001-45"));
        clientes.saveAll(base);

        Random rnd = new Random(42);
        List<String> motivos = ChamadoService.MOTIVOS;
        LocalDateTime agora = LocalDateTime.now();

        for (int i = 0; i < 90; i++) {
            Chamado c = new Chamado();
            // distribuição desigual para o gráfico "Top 5 clientes" ficar interessante
            int idx = Math.min(base.size() - 1, (int) (Math.abs(rnd.nextGaussian()) * 2));
            c.setCliente(base.get(idx));
            c.setMotivoAbertura(motivos.get(Math.min(motivos.size() - 1, (int) (Math.abs(rnd.nextGaussian()) * 2.2))));
            c.setDescricao(DESCRICOES[rnd.nextInt(DESCRICOES.length)]);
            c.setPrioridade(Prioridade.values()[rnd.nextInt(Prioridade.values().length)]);
            c.setDataAbertura(agora.minusDays(rnd.nextInt(30)).minusMinutes(rnd.nextInt(600)));

            int r = rnd.nextInt(10);
            StatusChamado status = r < 5 ? StatusChamado.FECHADO
                    : r < 7 ? StatusChamado.EM_ANDAMENTO
                    : r < 9 ? StatusChamado.ABERTO : StatusChamado.AGUARDANDO_CLIENTE;
            c.setStatus(status);

            if (status != StatusChamado.ABERTO) c.setTecnicoResponsavel(rnd.nextBoolean() ? t1 : t2);
            if (status == StatusChamado.FECHADO) {
                int tempo = 15 + rnd.nextInt(480);
                LocalDateTime fim = c.getDataAbertura().plusMinutes(tempo);
                c.setTempoAtendimentoMinutos(tempo);
                c.setDataFechamento(fim.isAfter(agora) ? agora : fim);
            }
            chamados.save(c);
        }
    }

    /** Cria o primeiro administrador sem senha padrão conhecida. */
    private void criarAdministradorInicial() {
        if (adminNome.isBlank() || adminEmail.isBlank() || adminSenha.length() < 12) {
            log.warn("Banco sem usuários. Defina BOOTSTRAP_ADMIN_NAME, BOOTSTRAP_ADMIN_EMAIL e "
                    + "BOOTSTRAP_ADMIN_PASSWORD (mínimo de 12 caracteres) e reinicie para criar o primeiro administrador.");
            return;
        }
        usuarios.save(novoUsuario(adminNome.trim(), adminEmail.trim().toLowerCase(), adminSenha, Perfil.ADMIN));
        log.info("Administrador inicial criado: {}", adminEmail.trim().toLowerCase());
    }

    /** Cadastra os motivos padrão e os já usados em chamados existentes (só se a tabela estiver vazia). */
    private void seedMotivos() {
        if (motivos.count() > 0) return;
        Set<String> nomes = new LinkedHashSet<>(ChamadoService.MOTIVOS);
        nomes.addAll(chamados.motivosDistintos());
        for (String nome : nomes) {
            MotivoChamado m = new MotivoChamado();
            m.setNome(nome);
            motivos.save(m);
        }
    }

    private Usuario novoUsuario(String nome, String email, String senha, Perfil perfil) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha(encoder.encode(senha));
        u.setPerfil(perfil);
        u.setAtivo(true);
        return u;
    }

    private Cliente novoCliente(String razao, String cnpj) {
        Cliente c = new Cliente();
        c.setRazaoSocial(razao);
        c.setCpfCnpj(cnpj);
        c.setEmailContato("contato@" + razao.split(" ")[0].toLowerCase().replaceAll("[^a-z]", "") + ".com.br");
        c.setTelefone("(62) 3000-0000");
        return c;
    }
}
