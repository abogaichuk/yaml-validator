package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Definition;
import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ValidationServiceImpl implements ValidationService {
    @Autowired
    private MessageProvider messageProvider;

    @Override
    public ValidationResult validate(final ObjectParameter definition, final List<Parameter> resources) {
        ValidationResult result = definition.validate();
//        definition.validate(resources)

//        definition.getChildren().stream()
//                .map(child -> {
//                    Parameter resource = findResourceByName(child.getName(), resources);
//                })
        return result;
    }

    private Parameter findResourceByName(String name, List<Parameter> resources) {
        return resources.stream()
                .filter(resource -> name.equalsIgnoreCase(resource.getName()))
                .findAny().orElse(null);
    }
}
