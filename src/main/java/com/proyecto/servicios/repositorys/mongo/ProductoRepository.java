package com.proyecto.servicios.repositorys.mongo;

import com.proyecto.servicios.entity.mongo.ProductoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends MongoRepository<ProductoDocument, Integer> {
}
