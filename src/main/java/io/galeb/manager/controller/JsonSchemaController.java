package io.galeb.manager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;

import io.galeb.manager.entity.AbstractEntity;

@RestController
@RequestMapping(value="/schema")
public class JsonSchemaController {

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/{model}", method = RequestMethod.GET)
    public ResponseEntity<String> schema(@PathVariable String model) throws Exception {
        String result;
        try {
            Class<?> clazz = Class.forName(AbstractEntity.class.getPackage().getName()+"."+model);
            ObjectMapper m = new ObjectMapper();
            SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
            m.acceptJsonFormatVisitor(m.constructType(clazz), visitor);
            JsonSchema jsonSchema = visitor.finalSchema();
            result = m.writerWithDefaultPrettyPrinter().writeValueAsString(jsonSchema);
        } catch (Exception ignore) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
