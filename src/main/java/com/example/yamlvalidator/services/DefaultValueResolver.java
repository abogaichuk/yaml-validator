package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Param;
import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.entity.Schema;
import com.example.yamlvalidator.grammar.KeyWord;
import com.example.yamlvalidator.utils.ValidatorUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public class DefaultValueResolver {

    public void fill(Schema schema, Resource resources) {
        collectDefaults(schema.getChildren())
                .forEach(paramDefault -> getAncestor(paramDefault)
                        .map(Param::getPath)
                        .flatMap(path -> findResourceByPath(path, resources))
                        .ifPresent(parentResource -> setValueIfMissed(paramDefault, parentResource))
                );
    }

    private Optional<Param> findResourceByPath(String path, Resource root) {
        return isEmpty(path) ? Optional.of(root) : root.deepSearch(path);
    }

    private void setValueIfMissed(Param defaultValue, Param parentResource) {
        parentResource
                .findChild(defaultValue.getParent().getName())
                .ifPresentOrElse(
                        p -> {}, //do nothing if param is found
                        () -> createResource(defaultValue, parentResource)
                );
    }

    private void createResource(Param defaultValue, Param parent) {
        var resource = new Resource(defaultValue.getParent().getName(), defaultValue.getValue(),
                parent, null, Param.YamlType.SCALAR);
        parent.addChild(resource);
    }

    private Optional<Param> getAncestor(Param param) {
        return Optional.ofNullable(param)
                .map(Param::getParent)
                .map(Param::getParent);
    }

    private Stream<Param> collectDefaults(List<Param> parameters) {
        return parameters.stream()
                .flatMap(parameter -> concat(of(parameter), collectDefaults(parameter.getChildren())))
                .map(parameter -> parameter.findChild(KeyWord.DEFAULT.name()))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
