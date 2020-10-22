package com.fiapcreditcard.clientBatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import java.util.Date;
import javax.sql.DataSource;

@SpringBootApplication
@EnableBatchProcessing

public class ClientBatchApplication {

	Logger logger = LoggerFactory.getLogger(ClientBatchApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ClientBatchApplication.class, args);
	}

	@Bean
	public FlatFileItemReader<Aluno> itemReader(@Value("${file.chunk}") Resource resource){
		return new FlatFileItemReaderBuilder<Aluno>()
				.name("Aluno item reader")
				.targetType(Aluno.class)
				.resource(resource)
				.delimited().delimiter(";").names("nome", "nregistro")
				.build();
	}

	@Bean
	public ItemProcessor<Aluno, Aluno> itemProcessor(){
		return (aluno) -> {
			aluno.setNome(aluno.getNome().toUpperCase());
			aluno.setNregistro(aluno.getNregistro());
			return aluno;
		};
	}

	@Bean
	public JdbcBatchItemWriter<Aluno> itemWriter(DataSource dataSource){
		return new JdbcBatchItemWriterBuilder<Aluno>()
				.dataSource(dataSource)
				.sql("insert into TB_ALUNO (nome, numero_cartao,ativo,data_atualizacao,data_criacao,saldo) values (:nome, :nregistro, true, null,CURRENT_TIMESTAMP(),5000)")
				.beanMapped()
				.build();
	}

	@Bean
	public Step step(StepBuilderFactory stepBuilderFactory,
					 ItemReader<Aluno> itemReader,
					 ItemProcessor<Aluno, Aluno> itemProcessor,
					 ItemWriter<Aluno> itemWriter){
		return stepBuilderFactory.get("Step chunk file -> mysql")
				.<Aluno, Aluno>chunk(2)
				.reader(itemReader)
				.processor(itemProcessor)
				.writer(itemWriter)
				.allowStartIfComplete(true)
				.build();
	}

	@Bean
	public Job job(JobBuilderFactory jobBuilderFactory,
				   Step step){
		return jobBuilderFactory.get("Job chunk")
				.start(step)
				.build();
	}

}
